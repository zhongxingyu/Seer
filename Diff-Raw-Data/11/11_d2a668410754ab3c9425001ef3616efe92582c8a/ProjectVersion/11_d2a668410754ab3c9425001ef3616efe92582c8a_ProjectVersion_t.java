 package pcc.vercon;
import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import pcc.io.IOUtils;
 
 
 public class ProjectVersion implements java.io.Serializable
 {
 	private static final long serialVersionUID = -6225543935740429316L;
 	private String m_number;
 	private String m_reason;
 	private String m_author;
 	private Date m_dateCommited;
 	private ArrayList<SourceFileRecord> m_files;
	private String m_projectName;
 	
	public  ProjectVersion(String p_projectName, String number, String author, String reason, ArrayList<String> filenames) throws IOException
 	{
 		m_number		=	(null == number) ? "None" : number;
 		m_author		=	(null == author) ? "None": author;
 		m_reason		=	(null == reason) ? "None" : reason;
 		m_files			=	new ArrayList<SourceFileRecord>();
 		m_dateCommited	=	new Date();
		m_projectName	=	p_projectName;
 		
 		for (int ii = 0; ii < filenames.size(); ii ++)
 		{
 			
			String[] lines = IOUtils.openSourceFile(m_projectName + File.separator + filenames.get(ii));
 			SourceFileRecord	srcFileRec	=	new SourceFileRecord (lines, filenames.get(ii));
 			m_files.add(srcFileRec);
 		}
 	}
 	
 	public String getNumber()
 	{
 		return (m_number);
 	}
 	
 	public String getReason()
 	{
 		return (m_reason);
 	}
 	
 	public String getAuthor()
 	{
 		return (m_author);
 	}
 	
 	public ArrayList<SourceFileRecord> getFiles()
 	{
 		return (m_files);
 	}
 	
 	public SourceFileRecord getFile(String name)
 	{
 		SourceFileRecord		retFileRec	=	null;
 		
 		for (int ii = 0; ii < m_files.size(); ii ++)
 		{
 			if (m_files.get(ii).getName().equalsIgnoreCase(name))
 			{
 				retFileRec	=	m_files.get(ii);
 				break;
 			}
 		}
 		
 		return (retFileRec);
 	}
 	
 	/**
 	 * number, reason, author
 	 */
 	public String getMetaData()
 	{
 		String	ret		=	"Version #: ".concat(m_number).concat(" ||| Reason: ").concat(m_reason).concat(" ||| Author: ").concat(m_author).concat(" ||| Date: ").concat(m_dateCommited.toString());	
 		
 		return (ret);
 	}
 }
