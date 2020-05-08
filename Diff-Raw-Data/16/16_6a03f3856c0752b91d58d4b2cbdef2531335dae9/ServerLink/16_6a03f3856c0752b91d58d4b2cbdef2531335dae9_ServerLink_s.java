 /*
  * File:   ServerLink.java
  * Author: Catherine
  *
  * Created on April 10, 2009, 2:55 AM
  *
  * This file is a part of Shoddy Battle.
  * Copyright (C) 2009  Catherine Fitzpatrick and Benjamin Gwin
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, visit the Free Software Foundation, Inc.
  * online at http://gnu.org.
  */
 
 package shoddybattleclient.network;
 import java.net.*;
 import java.io.*;
 import java.util.*;
 import java.nio.ByteBuffer;
 import java.util.concurrent.*;
 import java.security.*;
 import javax.crypto.Cipher;
 import javax.crypto.spec.SecretKeySpec;
 import shoddybattleclient.BattleWindow;
 import shoddybattleclient.ChatPane;
 import shoddybattleclient.GameVisualisation.VisualPokemon;
 import shoddybattleclient.LobbyWindow;
 import shoddybattleclient.ServerConnect;
 import shoddybattleclient.shoddybattle.Pokemon;
 import shoddybattleclient.shoddybattle.PokemonMove;
 import shoddybattleclient.shoddybattle.PokemonNature;
 import shoddybattleclient.shoddybattle.PokemonSpecies;
 import shoddybattleclient.utils.MoveListParser;
 import shoddybattleclient.utils.SpeciesListParser;
 import shoddybattleclient.utils.Text;
 
 /**
  * An instance of this class acts as the client's link to the Shoddy Battle 2
  * server.
  *
  * @author Catherine
  */
 public class ServerLink extends Thread {
 
     public interface ChallengeMediator {
         /**
          * Get the team being used for this challenge.
          */
         public Pokemon[] getTeam();
 
         /**
          * Called when the challenge has been resolved, either through it
          * being accepted or rejected. If it was accepted, the method should
          * send the client's team to the server.
          */
         public void informResolved(boolean accepted);
         
         /**
          * Get the name of the user who has been challenged.
          */
         String getOpponent();
 
         /**
          * Get the generation being played.
          */
         public int getGeneration();
 
         /**
          * Get the active party size ("n").
          */
         public int getActivePartySize();
 
         // TODO: Clauses
     }
 
     /**
      * Messages sent by the client to the server.
      */
     public static class OutMessage extends ByteArrayOutputStream {
         protected final DataOutputStream m_stream = new DataOutputStream(this);
         public OutMessage(int type) {
             try {
                 m_stream.write(type);
                 m_stream.writeInt(0); // insert in 0 for size for now
             } catch (IOException e) {
                 
             }
         }
         @Override
         public byte[] toByteArray() {
             byte[] bytes = super.toByteArray();
             ByteBuffer buffer = ByteBuffer.wrap(bytes);
             buffer.putInt(1, bytes.length - 5);
             return bytes;
         }
     }
 
     public static class RequestChallengeMessage extends OutMessage {
         public RequestChallengeMessage(String user) {
             super(0); // see network.cpp
             try {
                 m_stream.writeUTF(user);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class ChallengeResponseMessage extends OutMessage {
         public ChallengeResponseMessage(byte[] response) {
             super(1);
 
             try {
                 m_stream.write(response, 0, 16);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class RegisterAccountMessage extends OutMessage {
         public RegisterAccountMessage(String user, String password) {
             super (2);
 
             try {
                 m_stream.writeUTF(user);
                 m_stream.writeUTF(password);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class JoinChannel extends OutMessage {
         public JoinChannel(String channel) {
             super(3);
             try {
                 m_stream.writeUTF(channel);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class ChannelMessage extends OutMessage {
         public ChannelMessage(int channel, String message) {
             super(4);
             try {
                 m_stream.writeInt(channel);
                 m_stream.writeUTF(message);
             } catch (Exception e) {
                 
             }
         }
     }
 
     public static class ModeMessage extends OutMessage {
         public ModeMessage(int channel, String user, int mode, boolean enable) {
             super(5);
             try {
                 m_stream.writeInt(channel);
                 m_stream.writeUTF(user);
                 m_stream.write(mode);
                 m_stream.write(enable ? 1 : 0);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class OutgoingChallenge extends OutMessage {
         public OutgoingChallenge(ChallengeMediator mediator) {
             super(6);
             try {
                 m_stream.writeUTF(mediator.getOpponent());
                 m_stream.write(mediator.getGeneration());
                 m_stream.writeInt(mediator.getActivePartySize());
                 // TODO: clauses
             } catch (Exception e) {
 
             }
         }
     }
 
     public void writePokemon(Pokemon pokemon, DataOutputStream stream)
                 throws IOException {
         stream.writeInt(PokemonSpecies.getIdFromName(
                 m_speciesList, pokemon.species));
         stream.writeUTF(pokemon.nickname);
         stream.write(pokemon.shiny ? 1 : 0);
         stream.write(pokemon.gender.getValue());
         stream.writeInt(pokemon.level);
         stream.writeUTF(pokemon.item);
         stream.writeUTF(pokemon.ability);
         PokemonNature nature = PokemonNature.getNature(pokemon.nature);
         stream.writeInt(nature.getInternalValue());
         stream.writeInt(pokemon.moves.length);
         for (int i = 0; i < pokemon.moves.length; ++i) {
             stream.writeInt(PokemonMove.getIdFromName(
                     m_moveList, pokemon.moves[i]));
             stream.writeInt(pokemon.ppUps[i]);
         }
         for (int i = 0; i < Pokemon.STAT_COUNT; ++i) {
             stream.writeInt(pokemon.ivs[i]);
             stream.writeInt(pokemon.evs[i]);
         }
     }
 
     public void writeTeam(Pokemon[] team, DataOutputStream stream)
                 throws IOException {
         stream.writeInt(team.length);
         for (Pokemon i : team) {
             writePokemon(i, stream);
         }
     }
 
     public static class ResolveChallenge extends OutMessage {
         public ResolveChallenge(ServerLink link,
                 String opponent,
                 boolean accepted,
                 Pokemon[] team) {
             super(7);
             try {
                 m_stream.writeUTF(opponent);
                 m_stream.write(accepted ? 1 : 0);
                 if (accepted) {
                     link.writeTeam(team, m_stream);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     public static class ChallengeTeam extends OutMessage {
         public ChallengeTeam(ServerLink link,
                 String opponent,
                 Pokemon[] team) {
             super(8);
             try {
                 m_stream.writeUTF(opponent);
                 link.writeTeam(team, m_stream);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class BattleAction extends OutMessage {
         public BattleAction(int fid, int turnType, int index, int target) {
             super(10);
             try {
                 m_stream.writeInt(fid);
                 m_stream.write(turnType);
                 m_stream.write(index);
                 m_stream.write(target);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static abstract class MessageHandler {
         /**
          * Handle a message from the server by reading values from the
          * DataInputStream. The underlying InputStream is a byte array, not
          * a socket, so the method is unable to ruin the connection with
          * the server.
          */
         public abstract void handle(ServerLink link, DataInputStream is)
                 throws IOException;
     }
 
     /**
      * Messages _received_ from the server that need to be handled by the
      * client.
      *
      * Note that the codes from this enum MUST match the codes from the
      * OutMessage::TYPE enum in the server.
      */
     public static class ServerMessage {
         static {
             m_map = new HashMap<Integer, ServerMessage>();
 
             // WELCOME_MESSAGE
             new ServerMessage(0, new MessageHandler() {
                 // int32  : server version
                 // string : server name
                 // string : welcome message
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int version = is.readInt();
                     String name = is.readUTF();
                     String welcome = is.readUTF();
 
                     link.m_serverConnect =
                             new ServerConnect(link, name, welcome);
                     link.m_serverConnect.setVisible(true);
 
                     //System.out.println("Received WELCOME_MESSAGE.");
                     System.out.println("Server version: " + version);
                     //System.out.println("Server name: " + name);
                     //System.out.println("Welcome message: " + welcome);
                 }
             });
             
             // PASSWORD_CHALLENGE
             new ServerMessage(1, new MessageHandler() {
                 // byte[16] : the challenge
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     byte[] challenge = new byte[16];
                     is.readFully(challenge);
 
                     // decrypt the challenge
                     try {
                         Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
 
                         // pass 1
                         cipher.init(Cipher.DECRYPT_MODE, link.m_key[1]);
                         challenge = cipher.doFinal(challenge, 0, 16);
 
                         // pass 2
                         cipher.init(Cipher.DECRYPT_MODE, link.m_key[0]);
                         challenge = cipher.doFinal(challenge, 0, 16);
 
                         ByteBuffer buffer = ByteBuffer.wrap(challenge);
                         int r = buffer.getInt(0) + 1;
                         buffer.putInt(0, r);
 
                         // pass 1
                         cipher.init(Cipher.ENCRYPT_MODE, link.m_key[0]);
                         challenge = cipher.doFinal(challenge, 0, 16);
 
                         // pass 2
                         cipher.init(Cipher.ENCRYPT_MODE, link.m_key[1]);
                         challenge = cipher.doFinal(challenge, 0, 16);
                         
                         link.sendMessage(
                                 new ChallengeResponseMessage(challenge));
 
                         // don't keep the keys in memory indefinitely
                         link.m_key[0] = null;
                         link.m_key[1] = null;
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
             });
 
             // REGISTRY_RESPONSE
             new ServerMessage(2, new MessageHandler() {
                 // byte : type
                 // string : details
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int type = is.readUnsignedByte();
                     String details = is.readUTF();
 
                     ServerConnect conn = link.m_serverConnect;
 
                     // see network.cpp for these values
                     switch (type) {
                         case 0:
                             conn.informNameUnavailable();
                             break;
                         case 1:
                             conn.informRegisterSuccess();
                             break;
                         case 2:
                             conn.informInvalidName();
                             break;
                         case 3:
                             conn.informNameTooLong();
                             break;
                         case 4:
                             conn.informNonexistentAccount();
                             break;
                         case 5:
                             conn.informFailedChallenge();
                             break;
                         case 6:
                             conn.informUserBanned(details);
                             break;
                         case 7:
                             conn.informSuccessfulLogin();
                             break;
                     }
                 }
             });
 
             // CHANNEL_INFO
             new ServerMessage(4, new MessageHandler() {
                 // int32  : channel id
                 // byte   : channel info
                 // string : channel name
                 // string : channel topic
                 // int32  : channel flags
                 // int32  : number of users
                 // for each user:
                 //      string : name
                 //      int32  : flags
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int id = is.readInt();
                     int type = is.read();
                     String channelName = is.readUTF();
                     String topic = is.readUTF();
                     int channelFlags = is.readInt();
                     int count = is.readInt();
                     LobbyWindow.Channel channel =
                             new LobbyWindow.Channel(id, type, channelName,
                             topic, channelFlags);
                     for (int i = 0; i < count; ++i) {
                         String name = is.readUTF();
                         int flags = is.readInt();
                         channel.addUser(name, flags);
                     }
                     link.m_lobby.addChannel(channel);
                 }
             });
 
             // CHANNEL_JOIN_PART
             new ServerMessage(5, new MessageHandler() {
                 // int32 : channel id
                 // string : user
                 // byte : joining?
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int id = is.readInt();
                     String user = is.readUTF();
                     boolean join = (is.readByte() != 0);
                     link.m_lobby.handleJoinPart(id, user, join);
                 }
             });
 
             // CHANNEL_STATUS
             new ServerMessage(6, new MessageHandler() {
                 // int32 : channel id
                 // string : user
                 // int32 : flags
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int id = is.readInt();
                     String user = is.readUTF();
                     int flags = is.readInt();
                     link.m_lobby.handleUpdateStatus(id, user, flags);
                 }
             });
 
             // CHANNEL_LIST
             new ServerMessage(7, new MessageHandler() {
                 // int32 : number of channels
                 // for each channel:
                 //      string : name
                 //      byte   : type
                 //      string : topic
                 //      int32  : population
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int count = is.readInt();
                     for (int i = 0; i < count; ++i) {
                         String name = is.readUTF();
                         int type = is.read();
                         String topic = is.readUTF();
                         int population = is.readInt();
                         System.out.println(name + ", "
                                 + topic + ", " + population);
                     }
                 }
             });
 
             // CHANNEL_MESSAGE
             new ServerMessage(8, new MessageHandler() {
                 // int32 : channel id
                 // string : user
                 // string : message
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int id = is.readInt();
                     String user = is.readUTF();
                     String message = is.readUTF();
                     link.m_lobby.handleChannelMessage(id, user, message);
                 }
             });
 
             // INCOMING_CHALLENGE
             new ServerMessage(9, new MessageHandler() {
                 // string : user
                 // byte : generation
                 // int32 : active party size
                 // ... [ TODO: clauses ] ...
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     // todo: use all of the information
                     String user = is.readUTF();
                     int generation = is.read();
                     int partySize = is.readInt();
 
                     link.m_lobby.addChallenge(user, true,
                             generation, partySize);
                 }
             });
 
             // FINALISE_CHALLENGE
             new ServerMessage(10, new MessageHandler() {
                 // string : user
                 // byte : whether the challenge was accepted
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     String user = is.readUTF();
                     boolean accepted = (is.read() != 0);
 
                     ChallengeMediator mediator = link.m_challenges.get(user);
                     if (mediator != null) {
                         mediator.informResolved(accepted);
                     }
                     if (!accepted) {
                         link.m_challenges.remove(user);
                     }
                 }
             });
 
             // TODO : 11 (CHALLENGE_WITHDRAWN)
 
             // BATTLE_BEGIN
             new ServerMessage(12, new MessageHandler() {
                 // int32 : field id
                 // string : opponent
                 // byte : party
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int id = is.readInt();
                     String user = is.readUTF();
                     int party = is.read();
                     String[] users = null;
 
                     ChallengeMediator mediator = null;
 
                     if (party == 0) {
                         // we made the original challenge
                         mediator = link.m_challenges.get(user);
                         link.m_challenges.remove(user);
                         users = new String[] { link.m_name, user };
                     } else {
                         // we were challenged
                         mediator = link.m_lobby.getChallengeMediator(user);
                         link.m_lobby.cancelChallenge(user);
                         users = new String[] { user, link.m_name };
                     }
                     //TODO: send maximum team length
                     BattleWindow wnd = new BattleWindow(link,
                             id,
                             mediator.getActivePartySize(),
                             6,
                             party,
                             users,
                             mediator.getTeam());
 
                     link.m_battles.put(id, wnd);
 
                     wnd.setVisible(true);
                 }
             });
 
             // REQUEST_ACTION
             new ServerMessage(13, new MessageHandler() {
                  // int32 : field id
                  // byte  : slot of relevant pokemon
                  // byte  : position of relevant pokemon
                  // byte  : whether this is a replacement
                  // int32 : number of pokemon
                  // for each pokemon:
                  //      byte : whether it is legal to switch to this pokemon
                  // if not replacement:
                  //      byte : whether switching is legal
                  //      byte : whether there is a forced move
                  //      if not forced:
                  //          int32 : total number of moves
                  //          for each move:
                  //              byte : whether the move is legal
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
                     
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int slot = is.read();
                     int pos = is.read();
 
                     boolean replacement = (is.read() != 0);
                     int count = is.readInt();
                     boolean[] switches = new boolean[count];
                     for (int i = 0; i < count; ++i) {
                         switches[i] = (is.read() != 0);
                     }
                     if (replacement) {
                         wnd.requestReplacement();
                         wnd.setValidSwitches(switches);
                     } else {
                         wnd.requestAction(pos, slot);
                         boolean canSwitch = (is.read() != 0);
                         if (!canSwitch) {
                             Arrays.fill(switches, false);
                         }
                         wnd.setValidSwitches(switches);
                         boolean forced = (is.read() != 0);
                         wnd.setForced(forced);
                         if (!forced) {
                             count = is.readInt();
                             boolean[] legal = new boolean[count];
                             for (int i = 0; i < count; ++i) {
                                 legal[i] = (is.read() != 0);
                             }
                             wnd.setValidMoves(legal);
                         }
                     }
                 }
             });
 
             // BATTLE_POKEMON
             new ServerMessage(14, new MessageHandler() {
                  // int32 : field id
                  // for 0...1:
                  //     for 0...n-1:
                  //         int16 : species id
                  //         if id != -1:
                  //             byte : gender
                  //             byte : whether the pokemon is shiny
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int size = wnd.getPartySize();
 
                     VisualPokemon[][] pokemon = new VisualPokemon[2][size];
 
                     for (int i = 0; i < 2; ++i) {
                         for (int j = 0; j < size; ++j) {
                             short id = is.readShort();
                             if (id != -1) {
                                 int gender = is.read();
                                 boolean shiny = (is.read() != 0);
                                 VisualPokemon p = new VisualPokemon(
                                         PokemonSpecies.getNameFromId(
                                         link.m_speciesList, id),
                                         gender, shiny);
                                 pokemon[i][j] = p;
                             }
                         }
                     }
                     if (wnd.getParticipant() == 0) {
                         wnd.setPokemon(pokemon[0], pokemon[1]);
                     } else {
                         wnd.setPokemon(pokemon[1], pokemon[0]);
                     }
                 }
             });
 
             // BATTLE_PRINT
             new ServerMessage(15, new MessageHandler() {
                 // int32 : field id
                 // byte  : category
                 // int16 : message id
                 // byte  : number of arguments
                 // for each argument:
                 //     string : value of the argument
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int category = is.readUnsignedByte();
                     int msg = is.readShort();
                     int count = is.readUnsignedByte();
                     String[] args = new String[count];
                     for (int i = 0; i < count; ++i) {
                         args[i] = is.readUTF();
                     }
 
                     String message = Text.getText(category, msg, args, wnd);
                     wnd.addMessage(null, message, false);
                 }
             });
 
             // BATTLE_VICTORY
             new ServerMessage(16, new MessageHandler() {
                 // int32 : field id
                 // int16 : party id
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int party = is.readShort();
                     wnd.informVictory(party);
                 }
             });
 
             // BATTLE_USE_MOVE
             new ServerMessage(17, new MessageHandler() {
                 // int32 : field id
                 // byte : party
                 // byte : slot
                 // string : user [nick]name
                 // int16 : move id
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int party = is.read();
                     int slot = is.read();
                     String name = is.readUTF();
                     int idx = is.readShort();
 
                     String move =
                             PokemonMove.getNameFromId(link.m_moveList, idx);
                     name = Text.formatName(name, (party == wnd.getParty()));
                     move = "<font class='move'>" + move + "</font>";
                     
                     String message = Text.getText(4, 10,
                             new String[] { name, move });
 
                     wnd.addMessage(null, message, false);
                 }
             });
 
             // BATTLE_WITHDRAW
             new ServerMessage(18, new MessageHandler() {
                 // int32 : field id
                 // byte : party
                 // byte : slot
                 // string : user [nick]name
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int party = is.read();
                     int slot = is.read();
                     String name = is.readUTF();
 
                     boolean us = (party == wnd.getParty());
                     String trainer = Text.formatTrainer(wnd.getTrainer(party),
                             wnd.getParty(), party);
                     name = Text.formatName(name, us);
                     String message = Text.getText(4, 11,
                             new String[] { trainer, name });
                     wnd.addMessage(null, message, false);
                 }
             });
 
             // BATTLE_SEND_OUT
             new ServerMessage(19, new MessageHandler() {
                 // int32 : field id
                 // byte : party
                 // byte : slot
                 // byte : index
                 // string : user [nick]name
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int party = is.read();
                     int slot = is.read();
                     int index = is.read();
                     String name = is.readUTF();
 
                     wnd.sendOut(party, slot, index, name);
 
                     String trainer = Text.formatTrainer(wnd.getTrainer(party),
                             wnd.getParty(), party);
 
                     name = Text.formatName(name, wnd.getParty() == party);
 
                     String message = Text.getText(4, 12,
                             new String[] { trainer, name });
                     wnd.addMessage(null, message, false);
                 }
             });
 
             // BATTLE_HEALTH_CHANGE
             new ServerMessage(20, new MessageHandler() {
                 // int32  : field id
                 // byte   : party
                 // byte   : slot
                 // string : pokemon [nick]name
                 // int16  : delta health in [0, 48]
                 // int16  : new total health [0, 48]
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int party = is.read();
                     int slot = is.read();
                     String name = is.readUTF();
 
                     int delta = is.readShort();
                     int total = is.readShort();
 
                     int percent = 100 * delta / 48;
 
                     name = Text.formatName(name, wnd.getParty() == party);
 
                     String message = Text.getText(4, 13,
                             new String[] { name, percent + "%" });
                     wnd.addMessage(null, message, false);
                     wnd.updateHealth(party, slot, total);
                 }
             });
 
             // BATTLE_SET_PP
             new ServerMessage(21, new MessageHandler() {
                 // int32  : field id
                 // byte   : pokemon
                 // byte   : move
                 // byte   : pp
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int i = is.readUnsignedByte();
                     int j = is.readUnsignedByte();
                     int pp = is.readUnsignedByte();
 
                     wnd.setPp(i, j, pp);
                 }
             });
 
             // BATTLE_FAINTED
             new ServerMessage(22, new MessageHandler() {
                 // int32 : field id
                 // byte : party
                 // byte : slot
                 // string : user [nick]name
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int party = is.read();
                     int slot = is.read();
                     String name = is.readUTF();
                     name = Text.formatName(name, wnd.getParty() == party);
                     String message = Text.getText(4, 15, new String[] { name });
                     wnd.addMessage(null, message, false);
                 }
             });
 
             // BATTLE_BEGIN_TURN
             new ServerMessage(23, new MessageHandler() {
                 // int32 : field id
                 // int16 : turn count
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int count = is.readShort();
 
                    System.out.print("Turn " + count + " begins.");
                 }
             });
 
             // add additional messages here
         }
 
         private static Map<Integer, ServerMessage> m_map;
         private MessageHandler m_handler;
         ServerMessage(int code, MessageHandler handler) {
             m_handler = handler;
             m_map.put(code, this);
         }
         public void handle(ServerLink link, DataInputStream is)
                 throws IOException {
             m_handler.handle(link, is);
         }
         public static ServerMessage getMessage(int code) {
             return m_map.get(code);
         }
     }
 
     private BlockingQueue<OutMessage> m_queue =
             new LinkedBlockingQueue<OutMessage>();
     private Socket m_socket;
     private DataInputStream m_input;
     private DataOutputStream m_output;
     private SecretKeySpec[] m_key = new SecretKeySpec[2];
     private String m_name;
     private Thread m_messageThread;
     private ServerConnect m_serverConnect;
     private LobbyWindow m_lobby;
     private List<PokemonSpecies> m_speciesList;
     private List<PokemonMove> m_moveList;
     private Map<String, ChallengeMediator> m_challenges =
             new HashMap<String, ChallengeMediator>();
     private Map<Integer, BattleWindow> m_battles =
             new HashMap<Integer, BattleWindow>();
 
     public ServerLink(String host, int port)
             throws IOException, UnknownHostException {
         m_socket = new Socket(InetAddress.getByName(host), port);
         m_input = new DataInputStream(m_socket.getInputStream());
         m_output = new DataOutputStream(m_socket.getOutputStream());
     }
 
     public BattleWindow getBattle(int id) {
         return m_battles.get(id);
     }
 
     public void sendBattleMessage(int id, String message)
             throws ChatPane.CommandException {
         m_lobby.getChannel(id).getChatPane().sendMessage(message);
     }
 
     public LobbyWindow getLobby() {
         return m_lobby;
     }
 
     public void postChallenge(ChallengeMediator mediator) {
         m_challenges.put(mediator.getOpponent(), mediator);
         sendMessage(new OutgoingChallenge(mediator));
     }
 
     public void resolveChallenge(String opponent,
             boolean accepted,
             Pokemon[] team) {
         sendMessage(new ResolveChallenge(this, opponent, accepted, team));
     }
 
     public void postChallengeTeam(String opponent, Pokemon[] team) {
         sendMessage(new ChallengeTeam(this, opponent, team));
     }
 
     public void loadSpecies(String file) {
         SpeciesListParser slp = new SpeciesListParser();
         m_speciesList = slp.parseDocument(file);
     }
 
     public void loadMoves(String file) {
         MoveListParser mlp = new MoveListParser();
         m_moveList = mlp.parseDocument(file);
     }
 
     public void registerAccount(String user, String password) {
         sendMessage(new RegisterAccountMessage(user, password));
     }
 
     public void setLobbyWindow(LobbyWindow window) {
         m_lobby = window;
     }
 
     public void joinChannel(String name) {
         sendMessage(new JoinChannel(name));
     }
 
     public void sendChannelMessage(int id, String message) {
         sendMessage(new ChannelMessage(id, message));
     }
 
     public void sendSwitchAction(int fid, int idx) {
         sendMessage(new BattleAction(fid, 1, idx, -1));
     }
 
     public void sendMoveAction(int fid, int idx, int target) {
         sendMessage(new BattleAction(fid, 0, idx, target));
     }
 
     public void updateMode(int channel, String user, int mode, boolean enable) {
         sendMessage(new ModeMessage(channel, user, mode, enable));
     }
 
     public void attemptAuthentication(String user, String password) {
         m_name = user;
         try {
             MessageDigest digest = MessageDigest.getInstance("SHA-256");
             byte[] key = digest.digest(password.getBytes("ISO-8859-1"));
             m_key[0] = new SecretKeySpec(key, 0, 16, "AES");
             m_key[1] = new SecretKeySpec(key, 16, 16, "AES");
             sendMessage(new RequestChallengeMessage(user));
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Send a message to the server.
      * @param msg the message to send.
      */
     public void sendMessage(OutMessage msg) {
         try {
             m_queue.put(msg);
         } catch (InterruptedException e) {
             
         }
     }
 
     void spawnMessageQueue() {
         m_messageThread = new Thread(new Runnable() {
             public void run() {
                 while (!interrupted()) {
                     OutMessage msg;
                     try {
                         msg = m_queue.take();
                     } catch (InterruptedException e) {
                         return; // end the thread
                     }
                     byte bytes[] = msg.toByteArray();
                     try {
                         m_output.write(bytes);
                     } catch (IOException e) {
 
                     }
                 }
             }
         });
         m_messageThread.start();
     }
 
     public void close() {
         try {
             m_input.close();
         } catch (Exception e) {
 
         }
         try {
             m_output.close();
         } catch (Exception e) {
 
         }
     }
 
     /**
      * protocol is simple:
      *     byte type : type of message
      *     int32 length : length of message body
      *     byte[length] : message body
      */
     @Override
     public void run() {
         spawnMessageQueue();
         while (true) {
             try {
                 int type = m_input.read();
                 int length = m_input.readInt();
                 byte[] body = new byte[length];
                 m_input.readFully(body);
 
                 // find the right handler to call
                 final ServerMessage msg = ServerMessage.getMessage(type);
                 if (msg == null) {
                     // unknown message type - but we can skip over it and live
                     System.out.println("Unkown message type: " + type);
                     continue;
                 }
 
                 // call the handler
                 final DataInputStream stream = new DataInputStream(
                         new ByteArrayInputStream(body));
                 try {
                     java.awt.EventQueue.invokeAndWait(new Runnable() {
                         public void run() {
                             try {
                                 msg.handle(ServerLink.this, stream);
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
                         }
                     });
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
             } catch (IOException e) {
                 // fatal error - exit the while loop
                 break;
             }
         }
 
         // interrupt the message thread
         m_messageThread.interrupt();
     }
 
     public static void main(String[] args) throws Exception {
         ServerLink link = new ServerLink("localhost", 8446);
         link.attemptAuthentication("Catherine", "test");
         link.run(); // block
     }
 
 }
