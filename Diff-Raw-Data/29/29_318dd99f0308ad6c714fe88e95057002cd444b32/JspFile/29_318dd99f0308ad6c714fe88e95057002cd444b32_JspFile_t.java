 package com.dianping.dobby.ticket.page.home;
 
 public enum JspFile {
	VIEW("/jsp/ticket/home.jsp"),
 
	SUMMARY("/jsp/ticket/summary.jsp"),
 
 	;
 
 	private String m_path;
 
 	private JspFile(String path) {
 		m_path = path;
 	}
 
 	public String getPath() {
 		return m_path;
 	}
 }
