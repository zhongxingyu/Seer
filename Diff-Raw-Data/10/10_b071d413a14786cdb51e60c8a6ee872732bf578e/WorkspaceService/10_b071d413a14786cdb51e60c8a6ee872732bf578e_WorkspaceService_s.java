 package com.avalchev.ide.service;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.io.FileDeleteStrategy;
 
 import com.avalchev.ide.model.Project;
 import com.avalchev.ide.model.Resource;
 
 public class WorkspaceService {
 
 	private Path workspaceRoot;
 	
 	public WorkspaceService(String path) {
 		workspaceRoot = Paths.get(path);
 	}
 
 	/**
 	 * Returns user's root workspace directory.
 	 * 
 	 * @param userId user's id
 	 * @return user's workspace root directory.
 	 */
 	public String getUserWorkspace(String userId) {
 		return Paths.get(workspaceRoot.toString(), userId).toString();
 	}
 	
 	/**
 	 * Returns user project root directory.
 	 * 
 	 * @param userId user's id
 	 * @param projectName project name
 	 * @return path detonating project root directory
 	 */
 	public Path getProjectDirectory(String userId, String projectName) {
 		return Paths.get(getUserWorkspace(userId), projectName);
 	}
 	
 	/**
 	 * Method returns list with user's {@link Project}s. Project's do not 
 	 * contain any resources, just project information.
 	 * 
 	 * @param userId username
 	 * @return user's project list.
 	 */
 	public List<Project> getProjectList(String userId) {
 		final Path projectDir = Paths.get(workspaceRoot.toString(), userId);
 		final List<Project> projects = new ArrayList<>();
 		
 		try (DirectoryStream<Path> dstream = Files.newDirectoryStream(projectDir)) {
 	        for (final Path pDirectory : dstream) {
 	            if (!Files.isDirectory(pDirectory)) {
 	            	continue;
 	            }
 	            final Project project = new Project();
 	            project.setType("maven");
 	            project.setOwner(userId);
 	            
 	            project.setName(pDirectory.getName(pDirectory.getNameCount() - 1).toString());
 	            
 	            projects.add(project);
 	        }
 	    } catch (Exception e) {
 	    	//Do nothing, just return what's already added to the list.
 		} 
 		
 		return projects;		
 	}
 	
 	public Project getProject(String userId, String name) 
 	throws Exception {
 		Path projectDir = Paths.get(workspaceRoot.toString(), userId, name);
         Project project = new Project();
         project.setType("maven");
         project.setOwner(userId);
         
         project.setName(projectDir.getName(projectDir.getNameCount() - 1).toString());
         
 		Files.walkFileTree(projectDir, new ProjectDirectoryVisitor(project, projectDir));
 
 		return project;
 	}
 	
 	public void save(String userId, String projectName, String filePath, String content) {
 		final Path file =  Paths.get(getProjectDirectory(userId, projectName).toString(), filePath);
 		System.out.println(filePath);
 		 try(BufferedWriter writer = Files.newBufferedWriter(file, Charset.defaultCharset())){
 			 writer.write(content);
 			 writer.flush();
 		 }catch(IOException exception) {
 			 exception.printStackTrace();
		      System.out.println("Error writing to file");
 		 }
 	}
 	
 	public List<Project> getProject(String userId) 
 	throws Exception {
 		Path projectDir = Paths.get(workspaceRoot.toString(), userId);
 		List<Project> projects = new ArrayList<>();
 		try (DirectoryStream<Path> ds = Files.newDirectoryStream(projectDir)) {
 	        for (Path dir : ds) {
 	            if (!Files.isDirectory(dir)) {
 	            	continue;
 	            }
 	            Project project = new Project();
 	            project.setType("maven");
 	            project.setOwner(userId);
 	            
 	            project.setName(dir.getName(dir.getNameCount() - 1).toString());
 	            
 	    		Files.walkFileTree(dir, new ProjectDirectoryVisitor(project, dir));
 	            
 	            projects.add(project);
 	        }
 	    } catch (Exception e) {
 			
 		} 
 		
 		
 		return projects;
 	}
 	
 	/**
 	 * Creates resource with content. Only files are expected. For building
 	 * resource meta information (path, parent, etc) is used 
 	 * {@code #getResource(Path, Path)}
 	 * 
 	 * @param userId username
 	 * @param projectId project name
 	 * @param fileId file path
 	 * @return {@code Resource}
 	 * @throws Exception
 	 */
 	public Resource getFile(String userId, String projectId, String fileId) 
 	throws Exception {
 		final Path projectPath = Paths.get(workspaceRoot.toString(), userId, projectId);
 		final Path filePath = Paths.get(workspaceRoot.toString(), userId, projectId, fileId);
 		
 		byte[] data = Files.readAllBytes(filePath);
 		Resource file = getResource(projectPath, filePath);
 		file.setContent(new String(data));
 		return file;
 	}	
 	
 	public Resource createFile(String userId, String projectId, String type, String path) 
 	throws Exception {
 		final Path projectPath = Paths.get(workspaceRoot.toString(), userId, projectId);
 		final Path filePath = Paths.get(workspaceRoot.toString(), userId, projectId, path);
 		
 		if ("directory".equals(type)) {
 			Files.createDirectory(filePath);
 		} else {
 			Files.createFile(filePath);
 		}
 		return getResource(projectPath, filePath);
 	}	
 
 	public void deleteFile(String userId, String projectId, String path) 
 	throws Exception {
 		final Path filePath = Paths.get(workspaceRoot.toString(), userId, projectId, path);
 
 	    FileDeleteStrategy.FORCE.deleteQuietly(filePath.toFile());
 	}	
 	
 	
 	public static Resource getResource(Path projectPath, Path filePath) {
 		final Path resourcePath = projectPath.relativize(filePath);
 		
 		Resource resource = new Resource();
 		resource.setProjectId(projectPath.getName(projectPath.getNameCount() - 1).toString());
 		resource.setPath(resourcePath.toString().replaceAll("\\\\", "/"));
 		resource.setName(resourcePath.getName(resourcePath.getNameCount() - 1).toString());
 		if (resourcePath.getParent() != null) {
 			resource.setParentId(resourcePath.getParent().toString().replaceAll("\\\\", "/"));
 		}
 		if (Files.isDirectory(filePath)) {
 			resource.setType("directory");
 		}
 		if (Files.isRegularFile(filePath)) {
 			resource.setType("file");
 		}
 		
 
 		return resource;
 	}	
 }
