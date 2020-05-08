 package dk.itu.jesl.multic;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.*;
 
 public class Student {
     private String name = null;
     private double score = 0.0;
     private ArrayList<String> wrong = new ArrayList<String>();
     private ArrayList<String> incomplete = new ArrayList<String>();
     private ArrayList<String> pending = new ArrayList<String>();
     private ArrayList<String> missing = new ArrayList<String>();
 
     public static Student parse(BufferedReader r, Question[][] corr) throws IOException {
 	Student stud = new Student();
 	if (stud.parseNonstatic(r, corr)) { return stud; }
 	else { return null; }
     }
 
     public static Student parseF(BufferedReader r, Question[][] corr, String name) throws IOException {
 	Student stud = new Student();
         stud.name = name;
 	stud.parseNonstaticF(r, corr);
 	return stud;
     }
 
     public void reportScore(PrintWriter w) {
 	w.format("%s\t%f%s\n", name, score, (pending.size() > 0 ? " (pending!)" : ""));
     }
 	 
     public void reportDetail(PrintWriter w) {
 	w.format("%s: %f", name, score);
 	if (wrong.size() > 0) {
 	    w.format(" wrong");
 	    String delim = ": ";
 	    for (String q : wrong) {
 		w.format("%s%s", delim, q);
 		delim = ", ";
 	    }
 	    w.format(".");
 	}
 	if (incomplete.size() > 0) {
 	    w.format(" incomplete");
 	    String delim = ": ";
 	    for (String q : incomplete) {
 		w.format("%s%s", delim, q);
 		delim = ", ";
 	    }
 	    w.format(".");
 	}
 	if (pending.size() > 0) {
 	    w.format(" pending");
 	    String delim = ": ";
 	    for (String q : pending) {
 		w.format("%s%s", delim, q);
 		delim = ", ";
 	    }
 	    w.format(".");
 	}
 	if (missing.size() > 0) {
 	    w.format(" missing");
 	    String delim = ": ";
 	    for (String q : missing) {
 		w.format("%s%s", delim, q);
 		delim = ", ";
 	    }
 	    w.format(".");
 	}
 	w.println();
     }
 	 
     private boolean parseNonstatic(BufferedReader r, Question[][] corr) throws IOException {
 	String s;
 	do {
 	    s = r.readLine();
 	    if (s == null) { return false; }
 	    s = s.trim();
 	} while (s.length() == 0);
 	name = s;
 	try {
 	    for (int i = 0; i < corr.length; i++) {
 		try {
 		    parsePage(r.readLine(), corr[i]);
 		} catch (Err.FormatException fe) {
 		    throw fe.setPage(i + 1);
 		} catch (NumberFormatException nfe) {
 		    throw new Err.FormatException(nfe).setPage(i + 1);
 		}
 	    }
 	} catch (Err.FormatException fe) {
 	    throw fe.setSection(name);
 	}
 	return true;
     }
 
     private static final Pattern answerP = Pattern.compile("(\\S+) *");
 
     private void parsePage(String s, Question[] corr) {
 	Err.conf(s != null);
 	int lastEnd = 0;
 	Matcher m = answerP.matcher(s);
 	for (int i = 0; i < corr.length; i++) {
 	    Question q = corr[i];
 	    try {
 		Err.conf(m.find());
 		Err.conf(m.start() == lastEnd, m.start() + " " + lastEnd);
                 registerScore(q, m.group(1));
 		lastEnd = m.end();
 	    } catch (Err.FormatException fe) {
 		throw fe.setProblem(q.name());
 	    } catch (NumberFormatException nfe) {
 		throw new Err.FormatException(nfe).setProblem(q.name());
 	    }
 	}
 	Err.conf(lastEnd >= s.length(), "Too many answers?");
 	Err.conf(lastEnd <= s.length(), "Mismatch: " + lastEnd + ", " + s.length());
     }
 
     private Pattern flineP = Pattern.compile("([1-9]+)([a-zA-Z])\\s*:\\s*([a-dA-D]*)");
 
     private void parseNonstaticF(BufferedReader r, Question[][] corr) throws IOException {
 	try {
             int prevQMain = 0;
             int prevQSub = 0;
             String line;
             while ((line = r.readLine()) != null) {
                 line = line.trim();
                 if (line.length() == 0) continue; // blank line, skip
                 Matcher m = flineP.matcher(line);
                 if (!m.matches()) throw new Err.FormatException("Unrecognized answer line: " + line);
                 try {
                     int qnoMain;
                     try {
                         qnoMain = Integer.parseInt(m.group(1));
                     } catch (NumberFormatException nfe) {
                         throw new Err.FormatException(nfe);
                     }
                     int qnoSub = m.group(2).toLowerCase().charAt(0) - ('a' - 1);
                     
                     if (qnoMain < prevQMain) throw new Err.FormatException("Question out of sequence");
                    if (qnoMain > prevQMain) {
                        for (int i = prevQMain+1; i < qnoMain; i++) {
                            missing.add("" + i);
                         }
                         prevQSub = 0;
                     }
                     if (qnoSub < prevQSub) throw new Err.FormatException("Question out of sequence");
                     for (int i = prevQSub+1; i < qnoSub; i++) {
                        missing.add("" + qnoMain + (('a' - 1) + i));
                     }
                     registerScore(corr[qnoMain-1][qnoSub-1], m.group(3));
                     prevQMain = qnoMain;
                     prevQSub = qnoSub;
                 } catch (Err.FormatException fe) {
                     throw fe.setProblem(m.group(1) + m.group(2));
                 } catch (RuntimeException rte) {
                     throw new Err.FormatException(rte).setProblem(m.group(1) + m.group(2));
                 }
             }
            if (prevQMain > 0) {
                for (int i = prevQSub+1; i < corr[prevQMain-1].length; i++) {
                    missing.add("" + prevQMain + (('a' - 1) + i));
                 }
            }
            for (int i = prevQMain+1; i < corr.length; i++) {
                missing.add("" + i);
             }
 	} catch (Err.FormatException fe) {
 	    throw fe.setSection(name);
 	}
     }
 
     private void registerScore(Question q, String ans) {
         if (ans.length() == 0) {
             missing.add(q.name());
             return;
         }
         double qs = q.score(ans);
         if (Double.isNaN(qs)) {
             throw new Err.FormatException("Not a number");
             // pending.add(q.name()); 
         } else {
             if (qs < 0) { wrong.add(q.name()); }
             else if (qs < q.maxScore) { incomplete.add(q.name()); }
             score += qs * q.rescaleFactor();
         }
     }
 }
