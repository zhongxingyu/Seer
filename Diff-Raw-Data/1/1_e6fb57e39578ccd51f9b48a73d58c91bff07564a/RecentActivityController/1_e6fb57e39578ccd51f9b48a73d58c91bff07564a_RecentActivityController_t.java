 package com.robonobo.midas.controller;
 
 import java.io.*;
 import java.util.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.robonobo.midas.model.MidasPlaylist;
 import com.robonobo.remote.service.MidasService;
 
 @Controller
 public class RecentActivityController extends BaseController {
 	@Autowired
 	MidasService midas;
 	
 	/**
 	 * Gives stream ids that have been on recently-updated playlists, one per line (text/plain response type)
 	 */
 	@RequestMapping("/recent-activity")
 	public void showStreamIdsOnRecentPlaylists(@RequestParam(value = "maxAgeMs", required = false) Long maxAgeMs,
 			HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		long tenMinsInMs = (long) (10 * 60 * 1000);
 		if(maxAgeMs == null || maxAgeMs > tenMinsInMs)
 			maxAgeMs = tenMinsInMs;
 		List<MidasPlaylist> playlists = midas.getRecentPlaylists(maxAgeMs);
 		Set<String> streamIds = new HashSet<String>();
 		for (MidasPlaylist pl : playlists) {
 			streamIds.addAll(pl.getStreamIds());
 		}
 		resp.setStatus(HttpServletResponse.SC_OK);
 		resp.setContentType("text/plain");
 		PrintWriter out = new PrintWriter(resp.getOutputStream());
 		for (String sid : streamIds) {
 			out.println(sid);
 		}
		out.close();
 	}
 }
