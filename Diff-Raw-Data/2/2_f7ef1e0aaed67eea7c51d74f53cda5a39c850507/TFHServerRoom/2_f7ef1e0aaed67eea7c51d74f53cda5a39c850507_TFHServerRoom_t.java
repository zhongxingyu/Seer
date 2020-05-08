 package com.weigreen.poler;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Random;
 import java.util.Timer;
 
 
 public class TFHServerRoom implements Runnable
 {
     private int roomPort;
 
     private short pass = 0; // pass times
     private short totalPlayerNumber;
 
     // godCard
     private short callPlayerNumber; // the player number which call
     private short godCardSuit = 0;
     private short heap = 0;
 
     // play
     private short part = 0;
     private short initialCardSuit; // the first suit per stage
     private short[] stageCardIds = new short[4];
     private short initialPlayerNumber;
     private short easternHeap = 0;
     private short northernHeap = 0;
 
     private TFHServerRoomListener server;
 
 
     public TFHServerRoom(int port, String roomName)
     {
         server = new TFHServerRoomListener(port, this);
         new Thread(server).start();
 
         roomPort = port;
 
         totalPlayerNumber = -1;
     }
 
 
     public void haveNewData(TFHBridgeMain main)
     {
         System.out.println("haveNewData()");
 
         short command = main.getCommand();
 
         if(command == TFHComm.GOD_CARD_DATA)
         {
             System.out.println("TFHComm.GOD_CARD_DATA");
 
             TFHBridgeDataGodCard godCardData = (TFHBridgeDataGodCard) main.getData();
 
             short newCallPlayerNumber = godCardData.getPlayerNumber();
             short newGodCardSuit = godCardData.getGodCardSuit();
             short newHeap = godCardData.getHeap();
 
             if(newHeap == 0)
             {
                 System.out.println("Player passed");
 
                 pass++;
 
                 if(pass >= 3)
                 {
                     System.out.println("triple pass, over to call god card");
                     System.out.println("god card suit is " + godCardSuit + "\nheap is " + heap );
 
                     initialPlayerNumber = callPlayerNumber;
 
                     TFHBridgeDataGodCard newGodCardData = new TFHBridgeDataGodCard("OVER", callPlayerNumber, godCardSuit, heap);
                     TFHBridgeMain newMain = new TFHBridgeMain(TFHComm.GOD_CARD_DATA, newGodCardData);
 
                     server.sendToAll(newMain);
                 }
                 else
                 {
                     System.out.println("pass < 3");
 
                     TFHBridgeDataGodCard newGodCardData = new TFHBridgeDataGodCard("KEEP", newCallPlayerNumber, newGodCardSuit, newHeap);
                     TFHBridgeMain newMain = new TFHBridgeMain(TFHComm.GOD_CARD_DATA, newGodCardData);
 
                     server.sendToAll(newMain);
                 }
             }
             else
             {
                 System.out.println("no pass, keep call");
 
                 pass = 0;
 
                 callPlayerNumber = newCallPlayerNumber;
                 godCardSuit = newGodCardSuit;
                 heap = newHeap;
 
                 TFHBridgeDataGodCard newGodCardData = new TFHBridgeDataGodCard("KEEP", callPlayerNumber, godCardSuit, heap);
                 TFHBridgeMain newMain = new TFHBridgeMain(TFHComm.GOD_CARD_DATA, newGodCardData);
 
                 server.sendToAll(newMain);
             }
         }
 
 
         if(command == TFHComm.PLAYER_DATA)
         {
             System.out.println("TFHComm.PLAYER_DATA");
 
             TFHBridgeDataPlayer playerData = (TFHBridgeDataPlayer) main.getData();
 
             TFHBridgeDataRoom roomData = analyze(playerData);
             TFHBridgeMain newMain = new TFHBridgeMain(TFHComm.ROOM_DATA, roomData);
 
             server.sendToAll(newMain);
         }
 
         if(command == TFHComm.CARD_DATA)
         {
             System.out.println("TFHComm.CARD_DATA");
 
             TFHBridgeDataCard cardData = new TFHBridgeDataCard(Functions.dealCard());
             TFHBridgeMain newMain = new TFHBridgeMain(TFHComm.CARD_DATA, cardData);
 
             server.sendToAll(newMain);
         }
 
         if(command == TFHComm.ROOM_NEW_PLAYER)
         {
             totalPlayerNumber++;
 
             TFHBridgeDataNewPlayer newPlayerData = new TFHBridgeDataNewPlayer(totalPlayerNumber);
             TFHBridgeMain newMain = new TFHBridgeMain(TFHComm.ROOM_NEW_PLAYER, newPlayerData);
 
             server.sendToAll(newMain);
         }
     }
 
 
     private TFHBridgeDataRoom analyze(TFHBridgeDataPlayer playerData)
     {
         System.out.println("analyze()");
 
         short playerNumber = playerData.getPlayerNumber();
         short cardId = playerData.getCardId();
 
         if(++part == 1)
         {
             System.out.println("first player");
 
             initialCardSuit = (short)(cardId / 100);
             stageCardIds[playerNumber] = cardId;
 
             TFHBridgeDataRoom roomData = new TFHBridgeDataRoom("KEEP", initialPlayerNumber, (short) ((initialPlayerNumber + part) % 4), cardId, easternHeap, northernHeap);
             return roomData;
         }
         else if(part < 4)
         {
             System.out.println("other players");
 
             stageCardIds[playerNumber] = cardId;
 
             TFHBridgeDataRoom roomData = new TFHBridgeDataRoom("KEEP", initialPlayerNumber, (short) ((initialPlayerNumber + part) % 4), cardId, easternHeap, northernHeap);
             return roomData;
         }
         else
         {
             System.out.println("forth player, start a new game");
 
             part = 0;
 
             stageCardIds[playerNumber] = cardId;
 
             short winPlayerId = getWinPlayerNumber(stageCardIds);
 
             if(winPlayerId == 0 || winPlayerId == 2)
             {
                 System.out.println("east and west players WIN");
 
                 easternHeap++;
             }
             else
             {
                 System.out.println("north and south players WIN");
 
                 northernHeap++;
             }
 
            initialPlayerNumber = winPlayerId;

             TFHBridgeDataRoom roomData = new TFHBridgeDataRoom("START", winPlayerId, winPlayerId, cardId, easternHeap, northernHeap);
 
             return roomData;
         }
     }
 
 
     private short getWinPlayerNumber(short[] stageCardIds)
     {
         System.out.println("getWinPlayerNumber()");
 
         short[] cardSuit = new short[4];
         short[] cardNumber = new short[4];
 
         LinkedList<Short> godCardPlayerList = new LinkedList<Short>();
         LinkedList<Short> normalCardPlayerList = new LinkedList<Short>();
 
         System.out.println("get suits and numbers of cards");
         for(int i = 0; i < 4; i++)
         {
             cardSuit[i] = (short)(stageCardIds[i] / 100);
             cardNumber[i] = (short)(stageCardIds[i] % 100);
 
             if(cardSuit[i] == godCardSuit)
             {
                 godCardPlayerList.add((short)i);
             }
             else if(cardSuit[i] == initialCardSuit)
             {
                 normalCardPlayerList.add((short)i);
             }
         }
 
         if(!godCardPlayerList.isEmpty())
         {
             System.out.println("biggest card is god card");
 
             if(godCardPlayerList.size() > 1)
             {
                 ListIterator<Short> iterator = godCardPlayerList.listIterator();
 
                 List<Short> godCardNumbers = new ArrayList<Short>();
 
                 while(iterator.hasNext())
                 {
                     short next = iterator.next();
 
                     System.out.println("godCardNumber: " + cardNumber[next]);
 
                     godCardNumbers.add(cardNumber[next]);
                 }
 
                 short winCardId = (short)(godCardSuit * 100 + Collections.max(godCardNumbers));
 
                 System.out.println("winCardId is " + winCardId);
 
                 for(int i = 0; i < 4; i++)
                 {
                     if(stageCardIds[i] == winCardId)
                     {
                         System.out.println("winner is " + i);
 
                         return (short)i;
                     }
                 }
             }
             else
             {
                 return godCardPlayerList.get(0);
             }
         }
         else
         {
             if(normalCardPlayerList.size() > 1)
             {
                 ListIterator<Short> iterator = normalCardPlayerList.listIterator();
 
                 List<Short> normalCardNumbers = new ArrayList<Short>();
 
                 while(iterator.hasNext())
                 {
                     short next = iterator.next();
 
                     System.out.println("normalCardNumber: " + cardNumber[next]);
 
                     normalCardNumbers.add(cardNumber[next]);
                 }
 
                 short winCardId = (short)(initialCardSuit * 100 + Collections.max(normalCardNumbers));
 
                 System.out.println("winCardId is " + winCardId);
 
                 for(int i = 0; i < 4; i++)
                 {
                     if(stageCardIds[i] == winCardId)
                     {
                         System.out.println("winner is " + i);
 
                         return (short)i;
                     }
                 }
             }
             else
             {
                 return normalCardPlayerList.get(0);
             }
         }
         System.out.println("error happened, return -1");
 
         return -1; // error happened
     }
 
     @Override
     public void run()
     {
 
     }
 
 }
