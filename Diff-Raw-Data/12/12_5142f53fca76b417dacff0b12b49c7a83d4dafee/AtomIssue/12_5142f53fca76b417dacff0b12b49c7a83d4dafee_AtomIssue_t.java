 /*
  *  Copyright 2010 Heroic Efforts, LLC
  *  
  *  This file is part of Viable.
  *
  *  Viable is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Viable is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Viable.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.heroicefforts.viable.android.rep.it.gdata;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import net.heroicefforts.viable.android.Config;
 import net.heroicefforts.viable.android.dao.Issue;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import android.os.Build;
 import android.text.TextUtils;
 import android.util.Log;
 
 /**
  * This class extends issue to add handling of the Gdata Atom protocol.
  * 
  * @author jevans
  *
  */
 public class AtomIssue extends Issue
 {
 	private static final String TAG = "AtomIssue";
 
 	private static final String BOP = "<p>";
 	private static final String EOP = "</p>";
 	private static final String SALT_STACKTRACE = "<p>Stacktrace:  ";
 
 	private static final String TYPE_DEFECT = "Defect";
 
 	private static final String LABEL_AFFECTED_VERSION = "AffectedVersion";
 	private static final String LABEL_DEVICE = "Device";
 	private static final String LABEL_HASH = "Hash";
 	private static final String LABEL_MODEL = "Model";
 	private static final String LABEL_PRIORITY = "Priority";
 	private static final String LABEL_SDK = "SDK";
 	private static final String LABEL_TYPE = "Type";
 
 	
 	
 	public AtomIssue(Issue issue)
 	{
 		this.copy(issue);
 	}
 
 	public AtomIssue(String atomEntry)
 		throws SAXException, IOException, ParserConfigurationException
 	{
 		parse(atomEntry);
 	}
 	
 	protected void parse(String atomEntry)
 		throws SAXException, IOException, ParserConfigurationException
 	{
 		if(Config.LOGV)
 			Log.v(TAG, "Parsing:  " + atomEntry);
 		SAXParserFactory f = SAXParserFactory.newInstance();
 		SAXParser p = f.newSAXParser();
 		p.parse(new InputSource(new StringReader(atomEntry)), new IssueContentHandler(this));
 	}
 	
 	public String toString()
 	{
 		StringBuilder builder = new StringBuilder();
 		builder.append("<?xml version='1.0' encoding='UTF-8'?>");
 		builder.append("<entry xmlns='http://www.w3.org/2005/Atom' xmlns:issues='http://schemas.google.com/projecthosting/issues/2009'>");
 		builder.append("<title>").append(getSummary()).append("</title>");
 		builder.append("<content type='html'>").append(getBody()).append("</content>");
 		builder.append("<author><name>Anonymous</name></author>");
 		builder.append("<issues:stars>").append(getVotes()).append("</issues:stars>");
 		builder.append(getLabels());
 		builder.append("</entry>");
 
 		return builder.toString();
 	}
 
 	private Object getLabels()
 	{
 		StringBuilder labels = new StringBuilder();
 		appendLabel(labels, LABEL_TYPE, getType());
 		appendLabel(labels, LABEL_PRIORITY, getPriority());
 		appendLabel(labels, LABEL_HASH, getHash());
 		for(String version : getAffectedVersions())
 			appendLabel(labels, LABEL_AFFECTED_VERSION, version);
 		if(TYPE_DEFECT.equals(getType()))
 		{
 			appendLabel(labels, LABEL_MODEL, Build.MODEL);
 			appendLabel(labels, LABEL_DEVICE, Build.DEVICE);
 			appendLabel(labels, LABEL_SDK, String.valueOf(Build.VERSION.SDK_INT));
 		}
 				
 		return labels;
 	}
 
 	private void appendLabel(StringBuilder labels, String type, String value)
 	{
		if(!TextUtils.isEmpty(value))
 			labels.append("<issues:label>").append(type + "-" + value).append("</issues:label>");
 	}
 
 	private StringBuilder getBody()
 	{
 		StringBuilder body = new StringBuilder();
 		body.append("<![CDATA[<p>").append(getDescription()).append(EOP);
 		body.append(BOP).append("Affected Versions:  ").append(Arrays.asList(getAffectedVersions())).append(EOP);
 		if (getStacktrace() != null)
 			body.append(SALT_STACKTRACE).append(getStacktrace()).append(EOP);
 		body.append("]]>");
 		return body;
 	}
 
 	private static class IssueContentHandler extends AtomHandler
 	{
 		private Issue issue;
 		private ArrayList<String> affectedVersions = new ArrayList<String>();
 		private ArrayList<String> affectedDevices = new ArrayList<String>();
 		private ArrayList<String> affectedModels = new ArrayList<String>();
 		private ArrayList<String> affectedSDKs = new ArrayList<String>();
 
 		
 		public IssueContentHandler(Issue issue)
 		{
 			this.issue = issue;
 		}
 		
 		@Override
 		public void endElement(String uri, String localName, String qName) throws SAXException
 		{
 			super.endElement(uri, localName, qName);
 			
 			if("id".equals(localName))
 			{
 				issue.setIssueId(value.substring(value.lastIndexOf("/") + 1));
 			}
 			else if("title".equals(localName))
 			{
 				issue.setSummary(value.toString());
 			}
 			else if("content".equals(localName))
 			{
 				int idxStart = value.indexOf(SALT_STACKTRACE);
 				if (idxStart != -1)
 				{
 					int idxEnd = value.indexOf(EOP, idxStart);
 					issue.setStacktrace(value.substring(idxStart + SALT_STACKTRACE.length(), idxEnd));
 					issue.setDescription(value.substring(0, idxStart).replaceAll(BOP, "").replaceAll(EOP, EOL));
 				}
 				else
 					issue.setDescription(value.toString().replaceAll(BOP, "").replaceAll(EOP, EOL));
 			}
 			else if("published".equals(localName))
 			{
 				issue.setCreateDate(parseDate(value));
 			}
 			else if("updated".equals(localName))
 			{
 				issue.setModifiedDate(parseDate(value));
 			}
 			else if("label".equals(localName))
 			{
 				int idx = value.indexOf("-");
 				if(idx != -1)
 				{
 					String type = value.substring(0, idx);
 					String val = value.substring(idx + 1);
 					
 					if(LABEL_TYPE.equals(type))
 						issue.setType(val);
 					else if(LABEL_PRIORITY.equals(type))
 						issue.setPriority(val);
 					else if(LABEL_HASH.equals(type))
 						issue.setHash(val);
 					else if(LABEL_AFFECTED_VERSION.equals(type))
 						affectedVersions.add(val);
 					else if(LABEL_DEVICE.equals(type))
 						affectedDevices.add(val);
 					else if(LABEL_MODEL.equals(type))
 						affectedModels.add(val);
 					else if(LABEL_SDK.equals(type))
 						affectedSDKs.add(val);					
 				}
 			}
 			else if("state".equals(localName))
 			{
 				issue.setState(value.toString());
 			}
 		}
 	
 		@Override
 		public void endDocument() throws SAXException
 		{
 			issue.setAffectedVersions(affectedVersions.toArray(new String[affectedVersions.size()]));
 			issue.setAffectedDevices(affectedDevices.toArray(new String[affectedDevices.size()]));
 			issue.setAffectedModels(affectedModels.toArray(new String[affectedModels.size()]));
 			issue.setAffectedSDKs(affectedSDKs.toArray(new String[affectedSDKs.size()]));
 		}
 	
 	}
 	
 }
