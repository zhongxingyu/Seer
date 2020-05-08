 package com.louishong.ibg.weixin.command;
 
 public class TestWeixinShiftCommand {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			System.out.println(WeixinShiftCommand.execute("Today"));
 			System.out.println(WeixinShiftCommand.execute("Today"));
 			System.out.println(WeixinShiftCommand.execute("Today"));
 		} catch (Exception e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 }
