 package com.youbuy.service.core;
 
 import java.io.IOException;
 
 import com.youbuy.service.core.util.DBParserUtil;
 
 public class VanclService extends YoubuyService {
 
 	private StringBuffer url;
 	private VanclConfig vc;
 
 	public VanclService(VanclConfig vc) {
 		this.vc = vc;
 		url = new StringBuffer("http://p-api.gicp.net");
 	}
 
 	// to get the vancl sales volume
 	public String getWeeklyranking() {
 		url.append("/salesvolume/weeklyranking");
		return YoubuyService.handleGet(YoubuyService.handleArguments(url, vc));
 	}
 
 	public String getMalesRanking() {
 		url.append("/salesvolume/malesranking");
		return YoubuyService.handleGet(YoubuyService.handleArguments(url, vc));
 	}
 
 	public String getLadiesRanking() {
 		url.append("/salesvolume/ladiesranking");
		return YoubuyService.handleGet(YoubuyService.handleArguments(url, vc));
 	}
 
 	/**
 	 * for test the service
 	 * 
 	 * @throws IOException
 	 */
 	public static void main(String[] args) throws IOException {
 		VanclConfig vc = new VanclConfig();
 		vc.init();
 		vc.setT();
 		vc.setSign();
 		VanclService vs = new VanclService(vc);
 		String result = vs.getWeeklyranking();
 		System.out.println(result);
 		DBParserUtil.parseDealVancl(result);
 	}
 }
