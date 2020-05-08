 package vs.piratenpartei.ch.parser.redmine;
 
 import java.io.IOException;
 import java.text.ParseException;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import vs.piratenpartei.ch.app.redmine.IssueDetailItem;
 import vs.piratenpartei.ch.app.redmine.IssueItem;
 import vs.piratenpartei.ch.app.redmine.IssueItemCollection;
 import vs.piratenpartei.ch.app.redmine.JournalItem;
 import vs.piratenpartei.ch.app.redmine.JournalItemCollection;
 import vs.piratenpartei.ch.parser.AbstractXmlParser;
 import android.util.Log;
 
 public class RedmineParser extends AbstractXmlParser
 {
 	private static final String TAG = "vs.piratenpartei.ch.app.helpers.RedmineParser";
 	
 	public IssueItemCollection getIssuesList() throws XmlPullParserException, IOException
 	{
 		Log.d(TAG, "getIssueList()");
 		IssueItemCollection result = new IssueItemCollection();
 		this._parser.require(XmlPullParser.START_TAG, null, "issues");
 		while(this._parser.next() != XmlPullParser.END_TAG)
 		{
 			if(this._parser.getEventType() != XmlPullParser.START_TAG)
 			{
 				continue;
 			}
 			String tagName = this._parser.getName();
 			if(tagName.equals("issue"))
 			{
 				result.add(this.getIssueItem());
 			}
 			else
 			{
 				this.skip();
 			}
 		}
 		return result;
 	}
 	
 	public IssueDetailItem getIssueDetailItem() throws XmlPullParserException, IOException, ParseException
 	{
 		IssueDetailItem result = new IssueDetailItem();
 		this._parser.require(XmlPullParser.START_TAG, null, "issue");
 		while(this._parser.next() != XmlPullParser.END_TAG)
 		{
 			if(this._parser.getEventType() != XmlPullParser.START_TAG)
 			{
 				continue;
 			}
 			String tagName = this._parser.getName();
 			if(tagName.equals("author"))
 			{
 				result.setAuthor(this.getAttributeWithoutInnerXml(tagName, "name"));
 			}
 			else if(tagName.equals("assigned_to"))
 			{
 				result.setAssignedTo(this.getAttributeWithoutInnerXml(tagName, "name"));
 			}
 			else if(tagName.equals("priority"))
 			{
 				result.setPriority(this.getAttributeWithoutInnerXml(tagName, "name"));
 			}
 			else if(tagName.equals("start_date"))
 			{
 				result.setStartDate(RedmineParser.convertXmlDate(this.getText(tagName)));
 			}
 			else if(tagName.equals("due_date"))
 			{
 				result.setDueDate(RedmineParser.convertXmlDate(this.getText(tagName)));
 			}
 			else if(tagName.equals("created_on"))
 			{
 				result.setCreatedOn(RedmineParser.convertXmlDateTime(this.getText(tagName)));
 			}
 			else if(tagName.equals("updated_on"))
 			{
 				result.setUpdatedOn(RedmineParser.convertXmlDateTime(this.getText(tagName)));
 			}
 			else if(tagName.equals("description"))
 			{
 				result.setDescription(this.getText(tagName));
 			}
 			else if(tagName.equals("done_ratio"))
 			{
 				result.setProgress(this.getTextAsInt(tagName));
 			}
 			else if(tagName.equals("status"))
 			{
 				result.setStatus(this.getAttributeWithoutInnerXml(tagName, "name"));
 			}
 			else if(tagName.equals("estimated_hours"))
 			{
 				result.setEstimatedHours(this.getText(tagName));
 			}
 			else if(tagName.equals("journals"))
 			{
 				result.setJournal(this.getIssueJournal());
 			}
 			else
 			{
 				this.skip();
 			}
 		}
 		return result;
 	}
 	
 	public IssueItem getIssueItem() throws XmlPullParserException, IOException
 	{
 		IssueItem result = new IssueItem();
		this._parser.require(XmlPullParser.END_TAG, null, "issue");
 		while(this._parser.next() != XmlPullParser.END_TAG)
 		{
 			if(this._parser.getEventType() != XmlPullParser.START_TAG)
 			{
 				continue;
 			}
 			String tagName = this._parser.getName();
 			if(tagName.equals("id"))
 			{
 				result.setId(this.getTextAsInt(tagName));
 			}
 			else if(tagName.equals("subject"))
 			{
 				result.setSubject(this.getText(tagName));
 			}
 			else
 			{
 				this.skip();
 			}
 		}
 		return result;
 	}
 
 	public JournalItemCollection getIssueJournal() throws XmlPullParserException, IOException, ParseException
 	{
 		JournalItemCollection result = new JournalItemCollection();
 		this._parser.require(XmlPullParser.START_TAG, null, "journals");
 		while(this._parser.next() != XmlPullParser.END_TAG)
 		{
 			if(this._parser.getEventType() != XmlPullParser.START_TAG)
 			{
 				continue;
 			}
 			String name = this._parser.getName();
 			if(name.equals("journal"))
 			{
 				result.add(this.getIssueJournalItem());
 			}
 			else
 			{
 				this.skip();
 			}
 		}
 		return result;
 	}
 	
 	public JournalItem getIssueJournalItem() throws ParseException, XmlPullParserException, IOException
 	{
 		JournalItem result = new JournalItem();
 		this._parser.require(XmlPullParser.START_TAG, null, "journal");
 		while(this._parser.next() != XmlPullParser.END_TAG)
 		{
 			if(this._parser.getEventType() != XmlPullParser.START_TAG)
 			{
 				continue;
 			}
 			String name = this._parser.getName();
 			if(name.equals("user"))
 			{
 				result.setAuthor(this.getAttributeWithoutInnerXml(name, "name"));
 			}
 			else if(name.equals("notes"))
 			{
 				result.setNotes(this.getText(name));
 			}
 			else if(name.equals("created_on"))
 			{
 				result.setCreatedOn(RedmineParser.convertXmlDateTime(this.getText(name)));
 			}
 			else
 			{
 				this.skip();
 			}
 		}
 		return result;
 	}
 }
