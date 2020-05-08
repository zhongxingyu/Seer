 package com.viamep.richard.jgopherd;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 public class BuckGophermap extends Gophermap {
 
 	@Override
 	public ArrayList<GopherEntry> parse(String currentdir, InputStream inputstream) {
 		ArrayList<GopherEntry> al = new ArrayList<GopherEntry>();
 		BufferedReader br = new BufferedReader(new InputStreamReader(inputstream));
 		while (true) {
 			String line = "";
 			String[] split = new String[4];
 			try {
 				line = br.readLine();
 			} catch (Throwable e) {
 				line = "";
 			}
 			if ((line == "") || (line == null)) {
 				break;
 			} else {
 				split = line.split("\t");
 			}
 			char kind;
 			String title;
 			String destination;
 			String host;
 			int port;
 			try {
 				kind = split[0].charAt(0);
 				title = split[0].substring(1);
 			} catch (Throwable e) {
 				break;
 			}
 			try {
 				if (split[1].charAt(0) == '/') {
 					destination = split[1];
 				} else {
 					destination = currentdir+"/"+split[1];
 				}
 				if (destination == "") throw new RuntimeException();
 			} catch (Throwable e) {
 				destination = currentdir+"/"+title;
 				if ((kind == '1') && new File(Main.props.getPropertyString("root","gopherdocs")+destination).isDirectory()) {
 					File f = new File(Main.props.getPropertyString("root","gopherdocs")+destination+"/gophertag");
 					if ((f.exists()) && (f.isFile()) && (f.canRead())) {
 						try {
 							title = new BufferedReader(new InputStreamReader(new FileInputStream(f))).readLine();
 						} catch (Throwable e1) {
 							// do nothing
 						}
 					}
 				}
 			}
 			try {
 				host = split[2];
 			} catch (Throwable e) {
 				host = Main.props.getPropertyString("name","127.0.0.1");
 			}
 			try {
 				port = Integer.parseInt(split[3]);
 			} catch (Throwable e) {
				port = Main.props.getPropertyInt("port",70);
 			}
 			al.add(new GopherEntry(kind, title, host, port, destination));
 		}
 		return al;
 	}
 
 }
