 package models;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class TimeSheet {
 	
 	private int _sheetId;
 	private User _user;
 	private Client _client;
 	private ArrayList<DataEntry> _dataEntries;
 	private String _note;
 	private Date _createdDate;
 	private Date _editedDate;
 	
 	
 	// getters and setters
 	public int getSheetId()
 	{ return _sheetId; }
 	public void setSheetId(int sheetId)
 	{ _sheetId = sheetId; }
 	
 	
 	public User getUser()
 	{ return _user; }
 	public void setUser(User user)
 	{ _user = user; }
 	
 	
 	public Client getClient()
 	{ return _client; }
 	public void setClient(Client client)
 	{ _client = client; }
 	
 	
 	public ArrayList<DataEntry> getDataEntries()
 	{ return _dataEntries; }
 	public void setDataEntries(ArrayList<DataEntry> dataEntry)
 	{ _dataEntries = dataEntry; }
 	
 	
 	public String getNote()
 	{ return _note; }
 	public void setNote(String note)
 	{ _note = note; }
 	
 	public Date getCreatedDate()
 	{ return _createdDate; }
 	public void setCreatedDate(Date createdDate)
 	{ _createdDate = createdDate; }
 	
 	
 	public Date getEditedDate()
 	{ return _editedDate; }
 	public void setEditedDate(Date editedDate)
 	{ _editedDate = editedDate; }
 	// end getters and setters
 	
 	
 	// Constructor
 	public Timesheet(int sheetId, User user)
 	{
 		this._sheetId = sheetId;
 		this._user = user;
		_dataEntries = new ArrayList<DataEntry>();
 	}
 	
 	// add a data entry object to list of data entries
 	public void addDataEntry(DataEntry dataEntry)
 	{
 		_dataEntries.add(dataEntry);
 	}
 	
 	// remove a data entry object from list of data entries
 	public void removeDataEntry(DataEntry dataEntry)
 	{
 		_dataEntries.remove(dataEntry);
 	}
 }
