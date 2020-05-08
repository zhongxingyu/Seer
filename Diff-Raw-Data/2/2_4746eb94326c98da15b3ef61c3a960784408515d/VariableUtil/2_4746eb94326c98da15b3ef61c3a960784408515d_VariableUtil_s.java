 /*
  * Copyright (C) 2012 CyborgDev <cyborg@alta189.com>
  *
  * This file is part of CyborgFactoids
  *
  * CyborgFactoids is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CyborgFactoids is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.alta189.cyborg.factoids.handlers.util;
 
 import com.alta189.cyborg.api.util.StringUtils;
 import com.alta189.cyborg.factoids.FactoidContext;
 import com.alta189.cyborg.factoids.LocationType;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class VariableUtil {
 
 	private static final Pattern varPattern = Pattern.compile("%([0-9][0-9]{0,2}|10000)%");
 	private static final Pattern varPatternRange = Pattern.compile("%([0-9][0-9]{0,2}|10000)-([0-9][0-9]{0,2}|10000)%");
 	private static final Pattern varPatternInfinite = Pattern.compile("%([0-9][0-9]{0,2}|10000)-%");
 	private static final Pattern nickPattern = Pattern.compile("%nick%");
 	private static final Pattern chanPattern = Pattern.compile("%chan%");
 
 	public static String replaceVars(String raw, FactoidContext context) {
 		String[] args = context.getRawArgs().split(" ");
 		Matcher matcher = nickPattern.matcher(raw);
 		raw = matcher.replaceAll(context.getSender().getNick());
 		
 		if (context.getLocationType() == LocationType.CHANNEL_MESSAGE) {
 			matcher = chanPattern.matcher(raw);
 			raw = matcher.replaceAll(context.getChannel().getName());
 		}
 
 		matcher =  varPattern.matcher(raw);
 		while (matcher.find()) {
 			String match = matcher.group();
 			int index = Integer.valueOf(match.substring(1, match.length() - 1));
 			if (args.length - 1 >= index) {
 				raw = raw.replace(match, args[index]);
 			}
 		}
 
 		matcher =  varPatternInfinite.matcher(raw);
 		while (matcher.find()) {
 			String match = matcher.group();
 			int index = Integer.valueOf(match.substring(1, match.length() - 2));
 			if (args.length - 1 >= index) {
 				raw = raw.replace(match, StringUtils.toString(args, index, " "));
 			}
 		}
 
 		matcher =  varPatternRange.matcher(raw);
 		while (matcher.find()) {
 			String match = matcher.group();
 			int index = Integer.valueOf(match.substring(1, match.indexOf("-")));
 			int end = Integer.valueOf(match.substring(match.indexOf("-") + 1, match.length() - 1));
 			System.out.println("index = '" + index + "'");
 			System.out.println("end = '" + end + "'");
 			if (args.length - 1 >= index) {
 				raw = raw.replace(match, StringUtils.toString(args, index, end,  " "));
 			}
 		}
 
 		return raw;
 	}
 }
