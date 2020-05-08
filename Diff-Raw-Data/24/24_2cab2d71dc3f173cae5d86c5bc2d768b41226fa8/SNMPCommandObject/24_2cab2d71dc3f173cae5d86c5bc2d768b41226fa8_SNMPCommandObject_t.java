 /**
  * 
  * @author Kai - Ting (Danil) Ko
  *
  * Save view information as UDP packet
  */
 
 package snmpadapter;
 
import java.io.Serializable;

public class SNMPCommandObject implements Serializable 
{
	private static final long serialVersionUID = 3309788420118205529L;
 	private String mCommunity; 
 	private String mAddress;
 	private String mOption; 
 	private String mViewName;
 	private String mOID;
 	private String mCommand;
 	
 	public void setCommand(String pCommand)
 	{
 		mCommand = pCommand;
 	} 
 	
 	public String getCommand()
 	{
 		return mCommand;
 	} 
 	
 	public String getCommunity() 
 	{
 		return mCommunity;
 	}
 	public void setCommunity(String pCommunity) 
 	{
 		mCommunity = pCommunity;
 	}
 	public String getAddress() 
 	{
 		return mAddress;
 	}
 	public void setAddress(String pAddress) 
 	{
 		mAddress = pAddress;
 	}
 	public String getOption() 
 	{
 		return mOption;
 	}
 	public void setOption(String pOption) 
 	{
 		mOption = pOption;
 	}
 	public String getViewName() 
 	{
 		return mViewName;
 	}
 	public void setViewName(String pViewName) 
 	{
 		mViewName = pViewName;
 	}
 	public String getOID() 
 	{
 		return mOID;
 	}
 	public void setOID(String pOID) 
 	{
 		mOID = pOID;
 	}
 }  // class SNMPViewObject
