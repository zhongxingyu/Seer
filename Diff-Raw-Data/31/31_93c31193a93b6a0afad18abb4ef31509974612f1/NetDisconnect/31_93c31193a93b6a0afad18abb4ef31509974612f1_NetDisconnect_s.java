 package com.chess.genesis;
 
 class NetDisconnect implements Runnable
 {
 	public void run()
 	{
 	try {
		Thread.sleep(2040);
 		if (NetActive.get() < 1)
 			SocketClient.hard_disconnect();
 	} catch (InterruptedException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 }
