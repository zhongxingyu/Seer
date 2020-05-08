 package matchHistorian;
 
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 class Test {
 	static String httpDownload(String urlString) throws Exception {
 		URL url = new URL(urlString);
 		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
 		connection.setRequestMethod("GET");
 		InputStreamReader stream = new InputStreamReader(connection.getInputStream());
 		char[] buffer = new char[4096];
 		StringBuilder builder = new StringBuilder();
 		while(true) {
 			int bytesRead = stream.read(buffer, 0, buffer.length);
 			if(bytesRead <= 0)
 				break;
 			builder.append(buffer, 0, bytesRead);
 		}
 		String output = builder.toString();
 		return output;
 	}
 	
 	public static void httpTest() {
 		try {
 			System.out.println("Downloading data...");
 			long start = System.currentTimeMillis();
 			String content = httpDownload("http://www.lolking.net/summoner/euw/19531813");
 			long duration = System.currentTimeMillis() - start;
 			System.out.println("Length: " + content.length());
 			System.out.println("Duration: " + duration + " ms");
 			/*
			PrintWriter writer = new PrintWriter("Output/summoner.html");
 			writer.write(content);
 			writer.close();
 			*/
 			Pattern pattern = Pattern.compile(
 					"<div class=\\\"match_(win|loss)\\\" data-game-id=\\\"(\\d+)\\\">.+?" +
 					"url\\(\\/\\/lkimg\\.zamimg\\.com\\/shared\\/riot\\/images\\/champions\\/(\\d+)_92\\.png\\).+?" +
 					"<div style=\\\"font-size: 12px; font-weight: bold;\\\">(.+?)<\\/div>.+?" +
 					"data-hoverswitch=\\\"(\\d+\\/\\d+\\/\\d+ \\d+:\\d+(?:AM|PM) .+?)\\\">.+?" +
 					"<strong>(\\d+)</strong> <span style=\\\"color: #BBBBBB; font-size: 10px; line-height: 6px;\\\">Kills<\\/span><br \\/>.+?" +
 					"<strong>(\\d+)</strong> <span style=\\\"color: #BBBBBB; font-size: 10px; line-height: 6px;\\\">Deaths<\\/span><br \\/>.+?" +
 					"<strong>(\\d+)</strong> <span style=\\\"color: #BBBBBB; font-size: 10px; line-height: 6px;\\\">Assists<\\/span>.+?" +
 					"<strong>(\\d+)\\.(\\d)k</strong><div class=\\\"match_details_cell_label\\\">Gold</div>.+?" +
 					"<strong>(\\d+)</strong><div class=\\\"match_details_cell_label\\\">Minions<\\/div>",
 					Pattern.DOTALL
 			);
 			Matcher matcher = pattern.matcher(content);
 			SimpleDateFormat inputDateFormat = new SimpleDateFormat("MM/dd/yy hh:mmaa zzz");
 			SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 			outputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
 			int counter = 1;
 			while(matcher.find()) {
 				try {
 					String winLoss = matcher.group(1);
 					boolean win = winLoss.equals("win");
 					String gameIdString = matcher.group(2);
 					int gameId = Integer.parseInt(gameIdString);
 					String championIdString = matcher.group(3);
 					int championId = Integer.parseInt(championIdString);
 					String mode = matcher.group(4);
 					String dateString = matcher.group(5);
 					Date date = inputDateFormat.parse(dateString);
 					String killsString = matcher.group(6);
 					int kills = Integer.parseInt(killsString);
 					String deathsString = matcher.group(7);
 					int deaths = Integer.parseInt(deathsString);
 					String assistsString = matcher.group(8);
 					int assists = Integer.parseInt(assistsString);
 					String goldIntegerString = matcher.group(9);
 					int goldInteger = Integer.parseInt(goldIntegerString);
 					String goldFractionString = matcher.group(10);
 					int goldFraction = Integer.parseInt(goldFractionString);
 					int gold = goldInteger * 1000 + goldFraction * 100;
 					String minionsString = matcher.group(11);
 					int minions = Integer.parseInt(minionsString);
 					System.out.println("Game " + counter + ":");
 					System.out.println("Win: " + win);
 					System.out.println("Game ID: " + gameId);
 					System.out.println("Champion ID: " + championId);
 					System.out.println("Mode: " + mode);
 					System.out.println("Date: " + outputDateFormat.format(date));
 					System.out.println("K/D/A: " + kills + "/" + deaths + "/" + assists);
 					System.out.println("Gold: " + gold);
 					System.out.println("Minions: " + minions);
 				}
 				catch(NumberFormatException exception) {
 				}
 				counter++;
 			}
 		}
 		catch(Exception exception) {
 			exception.printStackTrace();
 		}
 	}
 }
