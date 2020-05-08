 /**
  *
  * Resume Maker
  * Copyright (c) 2011, Sandeep Gupta
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 package com.sangupta.resumemaker.github;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.egit.github.core.ExtendedRepositoryCommit;
 import org.eclipse.egit.github.core.Repository;
 import org.eclipse.egit.github.core.RepositoryCommit;
 import org.eclipse.egit.github.core.User;
 import org.eclipse.egit.github.core.service.CollaboratorService;
 import org.eclipse.egit.github.core.service.CommitService;
 import org.eclipse.egit.github.core.service.ExtendedCommitService;
 import org.eclipse.egit.github.core.service.OrganizationService;
 import org.eclipse.egit.github.core.service.RepositoryService;
 import org.eclipse.egit.github.core.service.UserService;
 
 import com.sangupta.resumemaker.Analyzer;
 import com.sangupta.resumemaker.model.Config;
 import com.sangupta.resumemaker.model.UserData;
 
 public class GitHubAnalyzer implements Analyzer {
 
 	private final CommitService commitService = new CommitService();
 	
 	private Connection conn = null;
 	
 	private PreparedStatement saveInDB = null;
 	
 	private PreparedStatement getFromDB = null;
 	
 	@Override
 	public void analyze(Config config, UserData userData) throws Exception {
 		// create the database
 		createDatabase();
 		
 		// start fetching data
 		final GitHubData data = userData.gitHubData;
 		
 		// get all repositories
 		UserService userService = new UserService();
 		final User currentUser = userService.getUser(config.gitHubConfig.userName);
 		
 		data.setLocation(currentUser.getLocation());
 		data.setBlogURL(currentUser.getBlog());
 		data.setFollowers(currentUser.getFollowers());
 		data.setJoiningDate(currentUser.getCreatedAt());
 		data.setWebURL(currentUser.getHtmlUrl());
 		
 		System.out.println("GitHub: user details fetched.");
 		
 		RepositoryService repositoryService = new RepositoryService();
 		List<Repository> userRepositories = repositoryService.getRepositories(config.gitHubConfig.userName);
 		
 		System.out.println("GitHub: user repositories fetched.");
 		
 		data.setPublicRepositories(userRepositories.size());
 		for(Repository repository : userRepositories) {
 			GitHubRepositoryData git = getGitHubRepositoryData(repository, true);
 			
 			getCommitDataForRepositoryAndUser(repository, currentUser, data);
 			
 			data.addRepository(git);
 		}
 		
 		// get all related organizations
 		OrganizationService organizationService = new OrganizationService();
 		List<User> orgUsers = organizationService.getOrganizations(config.gitHubConfig.userName);
 		List<Repository> orgRepositories = new ArrayList<Repository>();
 		for(User orgUser : orgUsers) {
 			List<Repository> repositories = repositoryService.getOrgRepositories(orgUser.getLogin());
 			if(repositories != null) {
 				orgRepositories.addAll(repositories);
 			}
 		}
 		
 		System.out.println("GitHub: user organizations fetched.");
 		
 		// check for which all repositories is the user a collaborator
 		CollaboratorService collaboratorService = new CollaboratorService();
 		int size = 0;
 		for(Repository repository : orgRepositories) {
 			List<User> users = collaboratorService.getCollaborators(repository);
 			
 			for(User user : users) {
 				if(user.getId() == currentUser.getId()) {
 					// this repository has this user as a collaborator
 					GitHubRepositoryData git = getGitHubRepositoryData(repository, false);
 					
 					getCommitDataForRepositoryAndUser(repository, currentUser, data);
 					
 					data.addRepository(git);
 					
 					size++;
 				}
 			}
 		}
 		data.setContributingRepositories(size);
 		
 		System.out.println("GitHub: user contributing repositories fetched.");
 		
 		// get the number of lines impacted by this user for all above repositories
 		// this is going to be a time consuming job
 		// we will use this data to plot graphs in the final resume sheet
 		
 		System.out.println("Done with github.");
 		
 		// close the database
 		shutDownDatabase();
 	}
 	
 	public void shutDownDatabase() {
 		if(conn != null) {
 			try {
 				conn.close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private GitHubRepositoryData getGitHubRepositoryData(Repository repository, boolean collaborated) {
 		GitHubRepositoryData git = new GitHubRepositoryData(false);
 		
 		git.setName(repository.getName());
 		git.setLanguage(repository.getLanguage());
 		git.setDescription(repository.getDescription());
 		git.setWatchers(repository.getWatchers());
 		git.setForks(repository.getForks());
 		git.setCreated(repository.getCreatedAt());
 		git.setLastPushed(repository.getPushedAt());
 		
 		return git;
 	}
 	
 	private void getCommitDataForRepositoryAndUser(Repository repository, User currentUser, GitHubData data) throws Exception {
 		final String name = repository.getName();
 		
 		System.out.print("GitHub: fetching details for repository " + name + "...");
 		
 		List<RepositoryCommit> commits = commitService.getCommits(repository);
 		int commitSize = commits.size();
 		
 		System.out.println("Commits for " + name + " = " + commitSize);
 		
 		// fetch the data for each commit
 		for(RepositoryCommit repositoryCommit : commits) {
 			if(repositoryCommit.getCommitter() != null && repositoryCommit.getCommitter().getId() == currentUser.getId()) {
 				// the user has committed this change
 				final String sha = repositoryCommit.getSha();
 				
 				GitHubCommitData commitData = null;
 				// check if the data is available in the local DB
 				// if yes, use it - else fetch fresh from server
 				commitData = getFromDatabase(name, sha);
 				if(commitData == null) {
 					ExtendedCommitService ecs = new ExtendedCommitService();
 					ExtendedRepositoryCommit commit = ecs.getCommit(repository, sha);
 		
 					commitData = getCommitData(name, sha, commit);
 					
 					saveInDatabase(commitData);
 				}
 				
 				data.addDetails(commitData);
 			}
 		}
 		
 		System.out.println("done!");
 	}
 
 	/**
 	 * Save commit data in a local database so that it can be reused again
 	 * and resume updates happen much faster.
 	 * 
 	 * @param commitData
 	 */
 	private void saveInDatabase(GitHubCommitData commitData) throws SQLException {
 		if(saveInDB == null) {
 			saveInDB = conn.prepareStatement("INSERT INTO commits (sha, repositoryName, additions, deletions, filesImpacted, createdAt) VALUES (?, ?, ?, ?, ?, ?)");
 		}
 		
 		saveInDB.setString(1, commitData.sha);
 		saveInDB.setString(2, commitData.repositoryID);
 		saveInDB.setInt(3, commitData.additions);
 		saveInDB.setInt(4, commitData.deletions);
 		saveInDB.setInt(5, commitData.filesImpacted);
 		
 		java.sql.Date d = null;
 		if(commitData.createdAt != null) {
 			d = new java.sql.Date(commitData.createdAt.getTime());
 		}
 		saveInDB.setDate(6, d);
 		
 		saveInDB.execute();
 	}
 
 	/**
 	 * Check if we have details on a particular SHA under the given repository name.
 	 * 
 	 * @param name
 	 * @param sha
 	 * @return
 	 * @throws SQLException 
 	 */
 	private GitHubCommitData getFromDatabase(String name, String sha) throws SQLException {
 		if(getFromDB == null) {
 			getFromDB = conn.prepareStatement("SELECT * FROM commits WHERE repositoryName = ? AND sha = ?");
 		}
 		
 		getFromDB.setString(1, name);
 		getFromDB.setString(2, sha);
 		
 		ResultSet rs = getFromDB.executeQuery();
 		if(rs.next()) {
 			GitHubCommitData cd = new GitHubCommitData();
 			cd.repositoryID = name;
 			cd.sha = sha;
 			cd.additions = rs.getInt("additions");
 			cd.deletions = rs.getInt("deletions");
 			cd.filesImpacted = rs.getInt("filesImpacted");
 			cd.createdAt = rs.getDate("createdAt");
 			
 			return cd;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Create a {@link GitHubCommitData} object from the given data source.
 	 * 
 	 * @param repositoryName
 	 * @param sha
 	 * @param commit
 	 * @return
 	 */
 	private GitHubCommitData getCommitData(String repositoryName, String sha, ExtendedRepositoryCommit commit) {
 		GitHubCommitData commitData = new GitHubCommitData();
 
 		commitData.repositoryID = repositoryName;
 		commitData.sha = sha;
		commitData.createdAt = commit.getCommitter().getCreatedAt();
 		commitData.additions = commit.getStats().getAdditions();
 		commitData.deletions = commit.getStats().getDeletions();
 		commitData.filesImpacted = commit.getFiles().size();
 		
 		return commitData;
 	}
 	
 	/**
 	 * Create the caching database used to store commit data locally.
 	 * 
 	 */
 	public void createDatabase() {
 		String driver = "org.sqlite.JDBC";
 		try {
 			Class.forName(driver).newInstance();
 
 			conn = DriverManager.getConnection("jdbc:sqlite:github.db");
 			
 			// create the database table
 			Statement stat = conn.createStatement();
 			stat.executeUpdate("CREATE TABLE IF NOT EXISTS commits (" +
 					"sha TEXT NOT NULL PRIMARY KEY," +
 					"repositoryName TEXT NOT NULL," +
 					"additions INTEGER," +
 					"deletions INTEGER," +
 					"filesImpacted INTEGER," +
 					"createdAt DATE)");
 			
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
