 package pcc.vercon;
 import java.io.IOException;
 import java.util.ArrayList;
 
 
 
 public class Project implements java.io.Serializable{
 	private static final long serialVersionUID = -8184397015459513852L;
 	private String latestVersion;
 	/**
 	 * version number -> project version file name
 	 */
 	private ArrayList<String> versions;
 	private ArrayList<String> sourceFileNames;
 	
 	private ArrayList<ProjectVersion>	m_projVersions;
 	
 	public  Project()
 	{
 		versions		=	new ArrayList<String>();
 		sourceFileNames	=	new ArrayList<String>();
 		m_projVersions	=	new ArrayList<ProjectVersion>();
 		
 	}
 	
 	public void commit(String number, String author, String reason) throws IOException
 	{
 		ProjectVersion	projVersion		=	new ProjectVersion (number, author, reason, sourceFileNames);
 		
 		m_projVersions.add(projVersion);
 		
 		versions.add(number);
 		latestVersion	=	number;
 
 	}
 	
 	public void addFile(String filename)
 	{
 		sourceFileNames.add(filename);
 	}
 	
	public void removeFile(String filename)
 	{
		sourceFileNames.remove(filename);
 	}
 	
 	public ArrayList<String> getFiles()
 	{
 		return (sourceFileNames);
 	}
 	
 	public String getCurrentVersion()
 	{
 		return latestVersion;
 	}
 	
 	public ProjectVersion getVersion(String number)
 	{
 		ProjectVersion		retProjVersion	=	null;
 		
 		for (int ii = 0; ii < m_projVersions.size(); ii ++)
 		{
 			if (m_projVersions.get(ii).getNumber().equalsIgnoreCase(number))
 			{
 				retProjVersion	=	m_projVersions.get(ii);
 				break;
 			}
 		}
 			
 
 		return retProjVersion;
 	}
 
 	public ArrayList<String> getVersionList()
 	{
 		return (versions);
 	}
 }
