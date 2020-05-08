 package model;
 
 import java.io.BufferedReader;
 import java.util.*;
 
 public class CommitModel
 {
 	private GitCommandLine _git = null;
 	private String _pathFilter = "";
 	
 	public CommitModel()
 	{
 		_git = new GitCommandLine();
 	}
 	
 	public Vector<Vector<String>> getCommitData(String reference)
 	{
 		Vector<Vector<String>> data = new Vector<Vector<String>>();
 		try
 		{
			String follow = (_pathFilter.length() > 0 ? "--follow":"");
			Vector<String> gitOutput = _git.execCommand("log "+follow+" --pretty=format:%h,%an,%ar,%s "+reference+" -- "+_pathFilter);
 			for (String line : gitOutput)
 			{
 				data.add(data.size(),new Vector<String>(Arrays.asList(line.trim().split(","))));
 			}
 		}
 		catch (Exception e) {}
 		return data;
 	}
 	
 	public void setPathFilter(String path)
 	{
 		if (path == null)
 		{
 			_pathFilter = "";
 		} else
 		{
 			_pathFilter = path;
 		}
 	}
 }
