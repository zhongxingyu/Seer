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
		String result =  YoubuyService.handleGet(YoubuyService.handleArguments(url, vc));
		url = new StringBuffer(url.substring(0,21));
		return result;
 	}
 
 	public String getMalesRanking() {
 		url.append("/salesvolume/malesranking");
		String result =  YoubuyService.handleGet(YoubuyService.handleArguments(url, vc));
		url = new StringBuffer(url.substring(0,21));
		return result;
 	}
 
 	public String getLadiesRanking() {
 		url.append("/salesvolume/ladiesranking");
		String result = YoubuyService.handleGet(YoubuyService.handleArguments(url, vc));
		url = new StringBuffer(url.substring(0,21));
		return result;
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
