 /*
  * CatalogDetail.java
  * 
  * Written by Kaiwen Xu (kevin) and Rendong Chen (ryan).
  * Released under Apache License 2.0.
  */
 
 package net.kevxu.purdueassist.course;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.kevxu.purdueassist.course.elements.Predefined.Subject;
 import net.kevxu.purdueassist.course.elements.Predefined.Term;
 import net.kevxu.purdueassist.course.elements.Predefined.Type;
 import net.kevxu.purdueassist.course.shared.CourseNotFoundException;
 import net.kevxu.purdueassist.course.shared.HtmlParseException;
 import net.kevxu.purdueassist.course.shared.RequestNotFinishedException;
 import net.kevxu.purdueassist.shared.httpclient.BasicHttpClientAsync;
 import net.kevxu.purdueassist.shared.httpclient.BasicHttpClientAsync.HttpRequestListener;
 import net.kevxu.purdueassist.shared.httpclient.HttpClientAsync.HttpMethod;
 import net.kevxu.purdueassist.shared.httpclient.MethodNotPostException;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.message.BasicNameValuePair;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 /**
  * This is the class implementing "Catalog Detail" search described in the
  * document. It utilizes asynchronous function call for non-blocking calling
  * style. You have to provide callback method by implementing
  * CatalogDetailListener.
  * <p>
  * Input: subject cnbr <br />
  * Input (optional): term
  * <p>
  * Output: <br />
  * subject cnbr name description levels type offeredBy department campuses
  * restrictions prerequisites
  * 
  * @author Rendong Chen (ryan), Kaiwen Xu (kevin)
  * @see CatalogDetailListener
  */
 public class CatalogDetail implements HttpRequestListener {
 
 	private static final String URL_HEAD = "https://selfservice.mypurdue.purdue.edu/prod/"
 			+ "bzwsrch.p_catalog_detail";
 
 	private Term term;
 	private Subject subject;
 	private int cnbr;
 
 	private CatalogDetailListener mListener;
 	private BasicHttpClientAsync httpClient;
 
 	private boolean requestFinished;
 
 	public interface CatalogDetailListener {
 		public void onCatalogDetailFinished(CatalogDetailEntry entry);
 
 		public void onCatalogDetailFinished(IOException e);
 
 		public void onCatalogDetailFinished(HtmlParseException e);
 
 		public void onCatalogDetailFinished(CourseNotFoundException e);
 
 		public void onCatalogDetailFinished(Exception e);
 	}
 
 	public CatalogDetail(CatalogDetailListener catalogDetailListener) {
 		this.mListener = catalogDetailListener;
 		this.requestFinished = true;
 	}
 
 	public void getResult(Subject subject, int cnbr)
 			throws RequestNotFinishedException {
 		getResult(Term.CURRENT, subject, cnbr);
 	}
 
 	public void getResult(Term term, Subject subject, int cnbr)
 			throws RequestNotFinishedException {
 		if (!this.requestFinished)
 			throw new RequestNotFinishedException();
 
 		this.requestFinished = false;
 
 		if (term == null)
 			term = Term.CURRENT;
 
 		this.term = term;
 		this.subject = subject;
 		this.cnbr = cnbr;
 
 		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 		parameters.add(new BasicNameValuePair("term", term.getLinkName()));
 		parameters.add(new BasicNameValuePair("subject", subject.name()));
 		parameters.add(new BasicNameValuePair("cnbr", Integer.toString(cnbr)));
 
 		httpClient = new BasicHttpClientAsync(URL_HEAD, HttpMethod.POST, this);
 		try {
 			httpClient.setParameters(parameters);
 			httpClient.getResponse();
 		} catch (MethodNotPostException e) {
 			e.printStackTrace();
		} finally {
			this.requestFinished = true;
 		}
 	}
 
 	/**
 	 * Check whether previous request has been finished.
 	 * 
 	 * @return Return true if previous request has already finished.
 	 */
 	public boolean isRequestFinished() {
 		return this.requestFinished;
 	}
 
 	@Override
 	public void onRequestFinished(HttpResponse httpResponse) {
 		try {
 			InputStream stream = httpResponse.getEntity().getContent();
 			Header encoding = httpResponse.getEntity().getContentEncoding();
 			Document document;
 			if (encoding == null) {
 				document = Jsoup.parse(stream, null, URL_HEAD);
 			} else {
 				document = Jsoup.parse(stream, encoding.getValue(), URL_HEAD);
 			}
 			stream.close();
 			CatalogDetailEntry entry = parseDocument(document);
 			mListener.onCatalogDetailFinished(entry);
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			mListener.onCatalogDetailFinished(e);
 		} catch (HtmlParseException e) {
 			mListener.onCatalogDetailFinished(e);
 		} catch (CourseNotFoundException e) {
 			mListener.onCatalogDetailFinished(e);
 		} catch (Exception e) {
 			mListener.onCatalogDetailFinished(e);
 		} finally {
 			this.requestFinished = true;
 		}
 	}
 
 	@Override
 	public void onRequestFinished(ClientProtocolException e) {
 		e.printStackTrace();
		this.requestFinished = true;
 	}
 
 	@Override
 	public void onRequestFinished(IOException e) {
 		mListener.onCatalogDetailFinished(e);
		this.requestFinished = true;
 	}
 
 	private CatalogDetailEntry parseDocument(Document document)
 			throws HtmlParseException, CourseNotFoundException, IOException {
 		CatalogDetailEntry entry = new CatalogDetailEntry(subject, cnbr);
 		Elements tableElements = document.getElementsByAttributeValue(
 				"summary",
 				"This table lists the course detail for the selected term.");
 		if (tableElements.isEmpty() != true) {
 			// get name
 			try {
 				Element body = tableElements.first().select("tbody").first();
 				String nameBlock = body.select("tr td.nttitle").first().text();
 				String[] temp = nameBlock.split(subject.name() + " "
 						+ String.valueOf(cnbr));
 				String name = temp[temp.length - 1].substring(3);
 				entry.setName(name);
 
 				// get description
 				body = body.select(".ntdefault").first();
 				String text = body.text();
 				int split = text.indexOf("Levels:");
 				String description = text.substring(0, split);
 				description = description.substring(20);
 				entry.setDescription(description);
 
 				// get levels
 				int begin = split;
 				int end = text.indexOf("Schedule Types:");
 				String levels = text.substring(begin + 8, end);
 				temp = levels.split("[ ,]");
 				List<String> lvs = new ArrayList<String>();
 				for (String s : temp)
 					if (!s.equals("")) {
 						lvs.add(s);
 					}
 				entry.setLevels(lvs);
 
 				// get type and prerequisites
 				List<Type> types = new ArrayList<Type>();
 				List<String> preq = new ArrayList<String>();
 				Elements parsing_A = body.select("a");
 				for (Element e : parsing_A) {
 					if (e.attr("href").contains("schd_in")
 							&& !(e.attr("href").contains("%"))) {
 
 						try {
 							types.add(Type.valueOf(e.text().replace(" ", "")));
 						} catch (Exception exception) {
 							throw new HtmlParseException();
 						}
 					} else if (e.attr("href").contains("sel_attr=")) {
 						preq.add(e.text());
 					}
 				}
 				if (types.size() > 0)
 					entry.setType(types);
 				if (preq.size() > 0)
 					entry.setPrerequisites(preq);
 
 				// get offered by
 				begin = text.indexOf("Offered By:");
 				end = text.indexOf("Department:");
 				if (end < 0)
 					end = text.indexOf("Course Attributes:");
 				if (end > 0) {
 					entry.setOfferedBy(text.substring(begin + 12, end - 1));
 				}
 
 				// get department
 				begin = text.indexOf("Department:");
 				if (begin > 0) {
 					end = text.indexOf("Course Attributes:");
 					entry.setDepartment((text.substring(begin + 12, end - 1)));
 				}
 
 				// get campus
 				begin = text
 						.indexOf("May be offered at any of the following campuses:");
 				String campuses;
 				end = text.indexOf("Repeatable for Additional Credit:");
 				if (end < 0)
 					end = text.indexOf("Learning Objectives:");
 				if (end < 0)
 					end = text.indexOf("Restrictions:");
 				if (end < 0)
 					end = text.indexOf("Corequisites:");
 				if (end < 0)
 					end = text.indexOf("Prerequisites:");
 				if (end < 0) {
 					campuses = text
 							.substring(begin
 									+ "May be offered at any of the following campuses:"
 											.length() + 5);
 				} else {
 					campuses = text
 							.substring(
 									begin
 											+ "May be offered at any of the following campuses:"
 													.length() + 5, end - 1);
 				}
 				temp = campuses.replace("    ", "#").split("#");
 				List<String> camps = new ArrayList<String>();
 				for (String s : temp) {
 					if (s.length() > 1) {
 						camps.add(s);
 					}
 
 				}
 				entry.setCampuses(camps);
 
 				// get restrictions
 				begin = text.indexOf("Restrictions:");
 				end = text.indexOf("Corequisites:");
 				if (end < 0)
 					end = text.indexOf("Prerequisites:");
 				if (begin > 0 && end < 0) {
 					entry.setRestrictions(text.substring(
 							begin + "Restrictions:".length()).replace(
 							"      ", "\n"));
 				} else if (begin > 0) {
 					entry.setRestrictions(text.substring(
 							begin + "Restrictions:".length(), end).replace(
 							"      ", "\n"));
 				}
 
 			} catch (StringIndexOutOfBoundsException e) {
 				// no type, not available
 				// System.out.println("-----------");
 				// System.out.println("Error for cnbr = " + cnbr);
 				// System.out.println("-----------");
 			}
 		} else {
 			throw new CourseNotFoundException();
 		}
 
 		return entry;
 	}
 
 	/**
 	 * This class contains information return by CatalogDetail.
 	 * 
 	 * @author Rendong Chen (ryan), Kaiwen Xu (kevin)
 	 */
 	@SuppressWarnings("unused")
 	public class CatalogDetailEntry {
 		private Subject searchSubject;
 		private int searchCnbr;
 
 		public CatalogDetailEntry(Subject subject, int cnbr) {
 			this.searchSubject = subject;
 			this.searchCnbr = cnbr;
 			this.cnbr = cnbr;
 			this.subject = subject;
 			name = null;
 			description = null;
 			levels = null;
 			type = null;
 			offeredBy = null;
 			department = null;
 			campuses = null;
 			restrictions = null;
 			prerequisites = null;
 
 		}
 
 		private Subject subject;
 		private int cnbr;
 		private String name;
 		private String description;
 		private List<String> levels;
 		private List<Type> type;
 		private String offeredBy;
 		private String department;
 		private List<String> campuses;
 		private String restrictions;
 		private List<String> prerequisites;
 
 		@Override
 		public String toString() {
 			StringBuffer myStr = new StringBuffer();
 			myStr.append("Subject: " + subject.toString() + "\n");
 			myStr.append("CNBR: " + cnbr + "\n");
 			if (name != null)
 				myStr.append("Name: " + name + "\n");
 			if (description != null)
 				myStr.append("Description: " + description + "\n");
 			if (levels != null) {
 				myStr.append("Level: ");
 				for (String s : levels)
 					myStr.append(s + " ; ");
 				myStr.append("\n");
 			}
 			if (type != null) {
 				myStr.append("Type: ");
 				for (Type t : type)
 					myStr.append(t.toString() + " ; ");
 				myStr.append("\n");
 			}
 			if (offeredBy != null)
 				myStr.append("OfferedBy: " + offeredBy + "\n");
 			if (department != null)
 				myStr.append("Department: " + department + "\n");
 			if (campuses != null) {
 				myStr.append("Campuses: ");
 				for (String s : campuses)
 					myStr.append(s + " ; ");
 				myStr.append("\n");
 			}
 			if (restrictions != null)
 				myStr.append("Restrictions: " + restrictions + "\n");
 			if (prerequisites != null) {
 				myStr.append("Prerequisites: ");
 				for (String s : prerequisites)
 					myStr.append(s + " ; ");
 				myStr.append("\n");
 			}
 			return myStr.toString();
 		}
 
 		private Subject getSearchSubject() {
 			return searchSubject;
 		}
 
 		private int getSearchCnbr() {
 			return searchCnbr;
 		}
 
 		public Subject getSubject() {
 			return subject;
 		}
 
 		public int getCnbr() {
 			return cnbr;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public void setName(String name) {
 			this.name = name;
 		}
 
 		public String getDescription() {
 			return description;
 		}
 
 		public void setDescription(String description) {
 			this.description = description;
 		}
 
 		public List<String> getLevels() {
 			return levels;
 		}
 
 		public void setLevels(List<String> levels) {
 			this.levels = levels;
 		}
 
 		public List<Type> getType() {
 			return type;
 		}
 
 		public void setType(List<Type> type) {
 			this.type = type;
 		}
 
 		public String getOfferedBy() {
 			return offeredBy;
 		}
 
 		public void setOfferedBy(String offeredBy) {
 			this.offeredBy = offeredBy;
 		}
 
 		public String getDepartment() {
 			return department;
 		}
 
 		public void setDepartment(String department) {
 			this.department = department;
 		}
 
 		public List<String> getCampuses() {
 			return campuses;
 		}
 
 		public void setCampuses(List<String> campuses) {
 			this.campuses = campuses;
 		}
 
 		public String getRestrictions() {
 			return restrictions;
 		}
 
 		public void setRestrictions(String restrictions) {
 			this.restrictions = restrictions;
 		}
 
 		public List<String> getPrerequisites() {
 			return prerequisites;
 		}
 
 		public void setPrerequisites(List<String> prerequisites) {
 			this.prerequisites = prerequisites;
 		}
 
 	}
 
 }
