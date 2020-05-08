 package com.papagiannis.tuberun.fetchers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import com.papagiannis.tuberun.LineType;
 import com.papagiannis.tuberun.Status;
 
 public class StatusesFetcher extends Fetcher {
 	// region private state
 	protected ArrayList<Status> _all_statuses = new ArrayList<Status>();
 	protected Date last_update;
 
 	public Boolean forWeekend() {
 		return statusurl == WEEKENDURL;
 	}
 
	 private final String NOWURL = "http://www.tfl.gov.uk/tfl/livetravelnews/realtime/tube/default.html";
 //	private final String NOWURL = "http://tuberun.dyndns.org:55559/getStatuses.php";
 	// private final String NOWURL = "http://localhost:55559/getStatuses.php";
 	private final String WEEKENDURL = "http://www.tfl.gov.uk/tfl/livetravelnews/realtime/track.aspx?offset=weekend";
 	protected String statusurl;
 
 	// region constructors
 	public StatusesFetcher() {
 	}
 
 	public static StatusesFetcher fetcherNowSigleton;
 	public static StatusesFetcher fetcherWeekendSigleton;
 
 	public static StatusesFetcher getInstance() {
 		return getInstance(false);
 	}
 
 	public static StatusesFetcher getInstance(Boolean forWeekend) {
 		if (!forWeekend) {
 			if (fetcherNowSigleton == null) {
 				fetcherNowSigleton = new StatusesFetcher();
 				fetcherNowSigleton.setNowUrl();
 			}
 			return fetcherNowSigleton;
 		} else {
 			if (fetcherWeekendSigleton == null) {
 				fetcherWeekendSigleton = new StatusesFetcher();
 				fetcherWeekendSigleton.setWeekendUrl();
 			}
 			return fetcherWeekendSigleton;
 		}
 	}
 
 	// region set service url
 	private void setNowUrl() {
 		statusurl = NOWURL;
 	}
 
 	private void setWeekendUrl() {
 		statusurl = WEEKENDURL;
 	}
 
 	@Override
 	public Date getUpdateTime() {
 		if (last_update == null)
 			last_update = new Date();
 		return (Date) last_update.clone();
 	}
 
 	// region issue request
 	public Status getStatus(LineType req) {
 		for (Status s : _all_statuses) {
 			if (s.line_name == req)
 				return s;
 		}
 		return null;
 	}
 
 	protected AtomicBoolean isFirst = new AtomicBoolean(true);
 	protected transient RequestTask task=null;
 	public void update() {
 		boolean first = isFirst.compareAndSet(true, false);
 		if (!first)
 			return; // only one at a time
 		task = new RequestTask(new HttpCallback() {
 			public void onReturn(String s) {
 				getStatusesCallBack(s);
 			}
 		});
 		task.setDesktopUserAgent();
 		task.execute(statusurl);
 	}
 
 	protected String beautify(String problem) {
 		problem = problem.replaceAll("<(.*?)>", " ");
 		problem = problem.replaceAll("(\\s)+", " ");
 		problem = problem.replaceAll("&nbsp;", " ");
 		return problem.trim();
 	}
 
 	protected void getStatusesCallBack(String reply) {
 		try {
 			reply = unCapitalizeTags(reply);
 			HashMap<String, String> result = new HashMap<String, String>();
 			HashMap<String, String> problems = new HashMap<String, String>();
 			String[] lines = new String[] { "bakerloo", "central", "circle",
 					"district", "hammersmithandcity", "jubilee",
 					"metropolitan", "northern", "piccadilly", "victoria",
 					"waterlooandcity", "dlr", "overground" };
 			for (int i = 0; i < lines.length; i++) {
 				String filter1 = "<h3 class=\"" + lines[i] + " ltn-name\">";
 				int j = reply.indexOf(filter1) + filter1.length();
 				if (j > filter1.length() /* sth found */) {
 					String rest = reply.substring(j);
 					String start = "<div class=\"status\">";
 					String start2 = "<div class=\"problem status\">";
 					String end = "</li>";
 					j = rest.indexOf(start);
 					int k = rest.indexOf(start2);
 					int e = rest.indexOf(end);
 					String status;
 					if (j < e || k < e) {
 						if (j < e)
 							rest = rest.substring(j + start.length());
 						else
 							rest = rest.substring(k + start2.length());
 						j = rest.indexOf("</div>");
 						String statust = rest.substring(0, j);
 						if (statust.contains("<h4")) {
 							String l = "<h4 class=\"ltn-title\">";
 							j = statust.indexOf(l) + l.length();
 							status = statust.substring(j);
 							j = status.indexOf("</h4>");
 							/*
 							 * k = status.IndexOf(","); if (k > 0 && k < j)
 							 * status = status.Substring(0, k); else
 							 */status = status.substring(0, j);
 						} else
 							status = statust;
 					} else
 						status = "Failed";
 					if (!status.equals("Good service")) {
 						j = rest.indexOf("<div class=\"message\">") + 21;
 						rest = rest.substring(j);
 						j = rest.indexOf("<a");
 						k = rest.indexOf("<A");
 						if (j == -1 && k > 0)
 							j = k;
 						String problem = rest.substring(0, j);
 						// must remove hmtl tags here.
 						problem = problem.replaceAll("\n", "");
 						problem = problem.replaceAll("&amp;", "and");
 						problems.put(lines[i], problem);
 					}
 					result.put(lines[i], status);
 				}
 			}
 			if (result.keySet().size() == 13) {
 				Date now = new Date();
 				// And lets write down the results
 				last_update = now;
 				_all_statuses = new ArrayList<Status>();
 				for (String name : result.keySet()) {
 					LineType ln = LineType.fromString(name);
 					String rstatus;
 					String rproblems;
 					if (result.containsKey(name))
 						rstatus = result.get(name);
 					else
 						throw new Exception("Failed to get a single status");
 					if (problems.containsKey(name))
 						rproblems = problems.get(name);
 					else
 						rproblems = "";
 					_all_statuses.add(new Status(ln, rstatus,
 							beautify(rproblems)));
 				}
 				notifyClients();
 			} else
 				throw new Exception("Wrong number of lines returned.");
 		} catch (Exception e) {
 			_all_statuses = new ArrayList<Status>();
 			notifyClients();
 		} finally {
 			isFirst.set(true);
 		}
 	}
 
 	private String unCapitalizeTags(String reply) {
 		StringBuilder res=new StringBuilder(reply.length());
 		boolean inTag=false;
 		for (int i=0;i<reply.length();i++) {
 			char c=reply.charAt(i);
 			switch (c) {
 			case '<':
 				inTag=true;
 				break;
 			case '>':
 			case ' ':
 				inTag=false;
 				break;
 			default:
 				if (inTag) c=Character.toLowerCase(c);
 				break;
 			}
 			res.append(c);
 		}
 		return res.toString();
 	}
 	
 	@Override
     public void abort() {
 		isFirst.set(true);
     	if (task!=null) task.cancel(true);
     }
 }
