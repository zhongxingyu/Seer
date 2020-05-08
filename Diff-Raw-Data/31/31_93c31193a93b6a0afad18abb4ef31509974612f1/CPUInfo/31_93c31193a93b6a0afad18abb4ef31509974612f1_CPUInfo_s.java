 package com.chess.genesis;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.IOException;
 
 class CPUInfo
 {
 	public static int getBogoMips()
 	{
 		int bogo = 500;
 		final String[] args = {"cat", "/proc/cpuinfo"};
 		final ProcessBuilder cmd = new ProcessBuilder(args);
 
 	try {
 		final Process process = cmd.start();
 		final InputStream in = process.getInputStream();
 		final BufferedReader buff = new BufferedReader(new InputStreamReader(in));
 
 		String line;
 		while ((line = buff.readLine()) != null) {
 			if (!line.matches(".*(BogoMIPS|bogomips).*"))
 				continue;
 			final String[] arr = line.trim().split("\\s");
 			bogo = Math.round(Float.parseFloat(arr[arr.length - 1]) / 10);
 			break;
 		}
 		in.close();
 		process.destroy();
 	} catch (IOException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 		return bogo;
 	}
 }
