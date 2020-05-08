 package com.viamep.richard.jgopherd;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.InetSocketAddress;
 import java.net.MalformedURLException;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class ClientThread extends Thread {
 	private Socket socket;
 	private OutputStream os;
 	private PrintWriter out;
 	private InputStream is;
 	private InputStreamReader isr;
 	private BufferedReader in;
 	private int scode;
 	private InetSocketAddress addr;
 	private String line;
 	private String params;
 	private String fline;
 	private boolean nodot = false;
 	private boolean http;
 	
 	public ClientThread(Socket sock) {
 		socket = sock;
 		try {
 			sock.setSoTimeout(Main.props.getPropertyInt("timeout",15));
 		} catch (Throwable e1) {
 			// do nothing
 		}
 		try {
 			os = sock.getOutputStream();
 			out = new PrintWriter(os,true);
 		} catch (Throwable e) {
 			Main.log.warning("Unable to create output stream: "+e.getMessage());
 			return;
 		}
 		try {
 			is = sock.getInputStream();
 			isr = new InputStreamReader(is);
 			in = new BufferedReader(isr);
 		} catch (Throwable e) {
 			Main.log.warning("Unable to create input stream: "+e.getMessage());
 			return;
 		}
 	}
 	public ClientThread(String ip, int port) {
 		try {
 			addr = new InetSocketAddress(ip,port);
 		} catch (Throwable e) {
 			addr = new InetSocketAddress("0.0.0.0",65535);
 		}
 	}
 	
 	public void run() {
 		addr = (InetSocketAddress)socket.getRemoteSocketAddress();
 		String source = addr.getHostName()+":"+addr.getPort();
 		line = "";
 		fline = "";
 		scode = 500;
 		boolean log = true;
 		http = false;
 		String httppath = "";
 		String httpparams = "";
 		String httpraw = "";
 		char httpkind = '1';
 		boolean nomole = false;
 		while (true) {
 			try {
 				line = in.readLine().replaceAll("\r","").replaceAll("\n","").replaceAll("\\.\\.","");
 			} catch (SocketTimeoutException e) {
 				continue;
 			} catch (Throwable e) {
 				continue;
 			}
 			if (!line.startsWith("/")) line = "/"+line;
 			fline = line;
 			String[] linex = line.split("\\?");
 			try {
 				line = linex[0];
 			} catch (Throwable e) {
 				line = "/";
 			}
 			try {
 				params = linex[1];
 			} catch (Throwable e) {
 				params = "";
 			}
 			if (line.startsWith("!nomole!")) {
 				nomole = true;
 				line = line.substring(5);
 			}
 			if (line.startsWith("/GET ") || line.startsWith("/POST ")) {
 				fline = fline.replaceFirst(" HTTP\\/.+$","");
 				try {
 					fline = URLDecoder.decode(fline,System.getProperty("file.encoding"));
 				} catch (Throwable e) {
 					// do nothing
 				}
 				// note to self: it's reparsing
 				linex = fline.split("\\?");
 				try {
 					line = linex[0];
 				} catch (Throwable e) {
 					line = "/";
 				}
 				try {
 					params = linex[1];
 				} catch (Throwable e) {
 					params = "";
 				}
 				log = false;
 				http = true;
 				int temp;
 				try {
 					temp = line.split(" ")[1].split("/")[1].length();
 				} catch (Throwable e) {
 					temp = 1;
 				}
 				if (temp > 1) {
 					httpraw = line.split(" ")[1];
 				} else {
 					try {
 						httppath = Util.GetArray(Util.GetArray(line.split(" "),1,0).split("/"),"/",2,0);
 					} catch (Throwable e) {
 						httppath = "/";
 					}
 					if (!httppath.startsWith("/")) httppath = "/"+httppath;
 					try {
 						httpkind = line.split(" ")[1].charAt(1);
 					} catch (Throwable e) {
 						httpkind = '1';
 					}
 					httpparams = params;
 				}
 			} else if (http && (line.equalsIgnoreCase("/"))) {
 				if ((httpraw != null) && (httpraw != "")) {
 					if (httpraw.equalsIgnoreCase("/robots.txt")) {
 						out.println("HTTP/1.1 200 OK");
 						out.println("Content-Type: text/plain");
 						out.println("");
 						out.println("User-agent: *");
 						out.println("Disallow: /");
 					}
 					break;
 				}
 				log = true;
 				scode = 200;
 				out.println("HTTP/1.1 200 OK");
 				out.println("Content-Type: "+Util.GetContentTypeForKind(httpkind));
 				out.println("");
 				ArrayList<GopherEntry> al = MakeEntries(httppath,httpparams,nomole);
 				if (httpkind == '1') {
 					out.println("<html><head><title>Gopher: "+Util.HTMLEscape(httppath)+"</title></head><body>");
 					out.println("<h2><a href=\"/1\">[/]</a> Gopher: "+Util.HTMLEscape(httppath)+"</h2><hr>");
 					out.println("<table border=\"0\"><tbody>");
 					for (GopherEntry ge : al) {
 						if (ge.kind == 'i') {
 							out.println("<tr><td>&nbsp;</td><td><pre>"+ge.title+"</pre></td></tr>");
 						} else if (ge.kind == '3') {
 							out.println("<tr><td>&nbsp;</td><td><pre><font color=\"red\"><b>"+ge.title+"</b></font></pre></td></tr>");
 						} else {
 							out.println("<tr><td><pre>"+Util.GetFullKind((ge.destination.startsWith("URL:")) ? 'U' : ge.kind)+"</pre></td><td><pre><a href=\""+((ge.host == Main.props.getPropertyString("name","127.0.0.1")) ? "http" : "gopher")+"://"+ge.host+":"+ge.port+"/"+ge.kind+ge.destination+"\">"+ge.title+"</a></pre></td></tr>");
 						}
 					}
 					out.println("</tbody></table>");
 					out.println("<hr><i>Generated by jgopherd v"+Main.version+" on "+Main.props.getPropertyString("name","127.0.0.1")+"</i>");
 					out.println("</body></html>");
 				}
 				break;
 			} else if (http) {
 				continue;
 			} else {
 				scode = 200;
 				for (GopherEntry ge : MakeEntries(line,params,nomole)) {
 					out.println(ge.GetAsRaw());
 				}
 				out.println(".");
 				break;
 			}
 		}
 		if (log) Main.log.finest((http ? "H" : "G")+" "+source+" "+scode+" "+(http ? httppath : line));
 		if (!http && !nodot) out.println(".");
 		try {
 			socket.close();
 		} catch (IOException e) {
 			// do nothing
 		}
 	}
 	
 	public ArrayList<GopherEntry> MakeError(String error, String[] details) {
 		ArrayList<GopherEntry> al = new ArrayList<GopherEntry>();
 		try {
 			al.add(new GopherEntry('3',error));
 			al.add(new GopherEntry('i'));
 			for (String det : details) {
 				al.add(new GopherEntry('i',det));
 			}
 		} catch (Throwable e) {
 			// do nothing
 		}
 		al.add(new GopherEntry('i'));
 		al.add(new GopherEntry('i',"Generated by jgopherd v"+Main.version+" on "+Main.props.getPropertyString("name","127.0.0.1")));
 		return al;
 	}
 	
 	public ArrayList<GopherEntry> MakeError(String error, Throwable e) {
 		StringWriter sw = new StringWriter();
 		e.printStackTrace(new PrintWriter(sw));
 		String[] sst = {"=== BEGIN STACK TRACE ==="};
 		String[] est = {"=== END STACK TRACE ==="};
 		return MakeError(error,Util.ConcatArrays(sst,sw.toString().split("\n"),est));
 	}
 	
 	public ArrayList<GopherEntry> MakeEntries(String line, String params, boolean nomole, String proxyip, int proxyport) {
 		if (addr == null) addr = new InetSocketAddress(proxyip,proxyport);
 		ArrayList<GopherEntry> al = new ArrayList<GopherEntry>();
 		File f = new File(Main.props.getPropertyString("root","gopherdocs")+line);
 		if (line.equalsIgnoreCase("/caps.txt")) {
 			scode = 200;
 			nodot = true;
 			al.add(new GopherEntry("CAPS"));
 			al.add(new GopherEntry("CapsVersion=1"));
 			al.add(new GopherEntry("ExpireCapsAfter=21600"));
 			al.add(new GopherEntry("PathDelimiter=/"));
 			al.add(new GopherEntry("PathIdentity=."));
 			al.add(new GopherEntry("PathParent=.."));
 			al.add(new GopherEntry("PathParentDouble=FALSE"));
 			al.add(new GopherEntry("PathEscapeCharacter=\\"));
 			al.add(new GopherEntry("PathKeepPreDelimiter=FALSE"));
 			al.add(new GopherEntry("ServerSoftware=jgopherd"));
 			al.add(new GopherEntry("ServerSoftwareVersion="+Main.version));
 			al.add(new GopherEntry("ServerArchitecture="+System.getProperty("os.arch")));
 			al.add(new GopherEntry("ServerDescription="+System.getProperty("os.name")+" v"+System.getProperty("os.version")+" running JVM "+System.getProperty("java.vendor")+" version "+System.getProperty("java.version")+" ("+System.getProperty("java.vendor.url")+")"));
 			al.add(new GopherEntry("ServerGeolocationString="));
 		} else if (f.isDirectory()) {
 			scode = 200;
 			FileInputStream fis;
 			try {
 				fis = new FileInputStream(Main.props.getPropertyString("root","gopherdocs")+line+"/gophermap");
 			} catch (Throwable e) {
 				al.add(new GopherEntry('i',"Directory listing for "+line));
 				al.add(new GopherEntry('i'));
 				File f1;
 				for (String fn : new File(Main.props.getPropertyString("root","gopherdocs")+line).list()) {
 					if (!fn.equalsIgnoreCase("gophermap")&&!fn.equalsIgnoreCase("gophertag")) {
 						f1 = new File(fn);
 						al.add(new GopherEntry(Util.GetType(Main.props.getPropertyString("root","gopherdocs")+line+"/"+f1.getName()),f1.getName(),(line.endsWith("/") ? line.substring(0,line.length()-1) : line)+"/"+f1.getName()));
 					}
 				}
 				al.add(new GopherEntry('i'));
 				al.add(new GopherEntry('i',"Generated by jgopherd v"+Main.version+" on "+Main.props.getPropertyString("name","127.0.0.1")));
 				return al;
 			}
 			return new BuckGophermap().parse(line,fis);
 		} else if (Util.IsExecutable(f) && !nomole) {
 			scode = 200;
 			ArrayList<String> envvars = new ArrayList<String>();
 			envvars.add("REMOTE_HOST="+addr.getHostName());
 			envvars.add("REMOTE_ADDR="+addr.getHostString());
 			envvars.add("REMOTE_PORT="+addr.getPort());
 			envvars.add("SERVER_HOST="+Main.props.getPropertyString("name","127.0.0.1"));
 			envvars.add("SERVER_PORT="+Main.props.getPropertyInt("port",70));
 			envvars.add("SELECTOR="+fline);
 			envvars.add("REQUEST="+line);
 			Process prc = null;
 			try {
 				String[] sa = {f.getAbsolutePath()};
 				prc = Runtime.getRuntime().exec(Util.ConcatArrays(sa,params.split(" ")),(String[])envvars.toArray(new String[envvars.size()]),f.getParentFile());
 			} catch (Throwable e) {
 				scode = 500;
 				return MakeError("Error while trying to execute mole",e);
 			}
 			al.addAll(new BuckGophermap().parse(line,prc.getInputStream()));
 		} else if (f.getName().endsWith(".jar") && f.exists() && !nomole) {
 			scode = 500;
 			URL url;
 			try {
 				url = f.toURI().toURL();
 			} catch (MalformedURLException e) {
 				return MakeError("Error while loading j-mole: Invalid URL",e);
 			}
 			URL[] urla = {url};
 			URLClassLoader ucl = new URLClassLoader(urla);
 			Thread.currentThread().setContextClassLoader(ucl);
 			Class<? extends JMole> cls;
 			try {
 				cls = (Class<? extends JMole>)Class.forName(f.getName().substring(0,f.getName().length()-4),true,ucl).asSubclass(JMole.class);
 			} catch (Throwable e) {
 				return MakeError("Error while loading j-mole: Invalid class",e);
 			}
 			HashMap<String,String> envmap = new HashMap<String,String>();
 			envmap.put("REMOTE_HOST",addr.getHostString());
 			envmap.put("REMOTE_ADDR",addr.getHostName());
 			envmap.put("REMOTE_PORT",""+addr.getPort());
 			envmap.put("SERVER_HOST",Main.props.getPropertyString("name","127.0.0.1"));
 			envmap.put("SERVER_PORT",""+Main.props.getPropertyInt("port",70));
 			envmap.put("SELECTOR",fline);
			envmap.put("PARAMS",fline);
 			envmap.put("REQUEST",line);
 			ArrayList<GopherEntry> entries;
 			try {
 				entries = cls.newInstance().run(envmap);
 			} catch (Throwable e) {
 				return MakeError("Error while executing j-mole",e);
 			}
 			scode = 200;
 			al.addAll(entries);
 		} else if (f.isFile()) {
 			nodot = true;
 			FileInputStream br;
 			try {
 				br = new FileInputStream(f);
 			} catch (Throwable e) {
 				return MakeError("Error while accessing file",e);
 			}
 			byte[] b = new byte[1];
 			try {
 				while (br.read(b) != -1) {
 					os.write(b);
 				}
 				out.flush();
 			} catch (Throwable e) {
 				e.printStackTrace();
 			}
 		} else {
 			if (line.equalsIgnoreCase("/")) {
 				al.add(new GopherEntry('3',"Welcome to jgopherd!"));
 				al.add(new GopherEntry('i'));
 				al.add(new GopherEntry('i',"This is a new installation of jgopherd on "+Main.props.getPropertyString("name","127.0.0.1")+"."));
 				al.add(new GopherEntry('i',"There is currently no content to be served from this installation."));
 				al.add(new GopherEntry('i'));
 				al.add(new GopherEntry('i',"Administrator: To start using the server, place a gophermap file"));
 				al.add(new GopherEntry('i',"on the gopherdocs directory (or the directory you have configured)"));
 				al.add(new GopherEntry('i',"formatted as a Bucktooth gophermap file. After the file is found,"));
 				al.add(new GopherEntry('i',"this message will disappear and the gophermap will be used."));
 				al.add(new GopherEntry('i'));
 				al.add(new GopherEntry('i',"Generated by jgopherd v"+Main.version+" on "+Main.props.getPropertyString("name","127.0.0.1")));
 			} else {
 				scode = 404;
 				String[] sa = {"The specified resource "+line+" was not found on this server."};
 				return MakeError("Resource not found",sa);
 			}
 		}
 		return al;
 	}
 	
 	public ArrayList<GopherEntry> MakeEntries(String line, String params, boolean nomole) {
 		return MakeEntries(line,params,nomole,"0.0.0.0",65535);
 	}
 }
