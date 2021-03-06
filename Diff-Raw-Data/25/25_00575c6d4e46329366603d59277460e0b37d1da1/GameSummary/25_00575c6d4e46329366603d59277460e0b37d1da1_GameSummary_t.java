 /* -*- compile-command: "cd ../../../../../../; ant debug install"; -*- */
 /*
  * Copyright 2009 - 2012 by Eric House (xwords@eehouse.org).  All
  * rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as
  * published by the Free Software Foundation; either version 2 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package org.eehouse.android.xw4.jni;
 
 import android.content.Context;
 import android.text.TextUtils;
 import junit.framework.Assert;
 import org.eehouse.android.xw4.R;
 import org.eehouse.android.xw4.Utils;
 
 
 /** Info we want to access when the game's closed that's not available
  * in CurGameInfo
  */
 public class GameSummary {
 
     public static final int MSG_FLAGS_NONE = 0;
     public static final int MSG_FLAGS_TURN = 1;
     public static final int MSG_FLAGS_CHAT = 2;
     public static final int MSG_FLAGS_GAMEOVER = 4;
     public static final int MSG_FLAGS_ALL = 7;
 
     public int nMoves;
     public int turn;
     public int nPlayers;
     public int missingPlayers;
     public int[] scores;
     public boolean gameOver;
     public String[] players;
     public CommsAddrRec.CommsConnType conType;
     public String smsPhone;
     // relay-related fields
     public String roomName;
     public String relayID;
     public int seed;
     public int pendingMsgLevel;
     public long modtime;
 
     public int dictLang;
     public CurGameInfo.DeviceRole serverRole;
 
     private int m_giFlags;
     private String m_playersSummary;
     private CurGameInfo m_gi;
     private Context m_context;
 
     public GameSummary( Context context ) {
         m_context = context;
         pendingMsgLevel = 0;
     }
 
     public GameSummary( Context context, CurGameInfo gi )
     {
         this( context );
         nPlayers = gi.nPlayers;
         dictLang = gi.dictLang;
         serverRole = gi.serverRole;
         m_gi = gi;
     }
 
     public boolean inNetworkGame()
     {
         return null != relayID;
     }
 
     public String summarizePlayers()
     {
         String result;
         if ( null == m_gi ) {
             result = m_playersSummary;
         } else {
             String[] names = new String[nPlayers];
             for ( int ii = 0; ii < nPlayers; ++ii ) {
                 names[ii] = m_gi.players[ii].name;
             }
             result = TextUtils.join( "\n", names );
             m_playersSummary = result;
         }
         return result;
     }
 
     public void readPlayers( String playersStr ) 
     {
         if ( null != playersStr ) {
             players = new String[nPlayers];
             String sep;
             if ( playersStr.contains("\n") ) {
                 sep = "\n";
             } else {
                 sep = m_context.getString( R.string.vs_join );
             }
 
             int ii, nxt;
             for ( ii = 0, nxt = 0; ; ++ii ) {
                 int prev = nxt;
                 nxt = playersStr.indexOf( sep, nxt );
                 String name = -1 == nxt ?
                     playersStr.substring( prev ) : 
                     playersStr.substring( prev, nxt );
                 players[ii] = name;
                 if ( -1 == nxt ) {
                     break;
                 }
                 nxt += sep.length();
             }
         }
     }
 
     public void setPlayerSummary( String summary ) 
     {
         m_playersSummary = summary;
     }
 
     public String summarizeState()
     {
         String result = null;
         if ( gameOver ) {
             result = m_context.getString( R.string.gameOver );
         } else {
             result = String.format( m_context.getString(R.string.movesf),
                                     nMoves );
         }
         return result;
     }
 
     public String summarizeRole()
     {
         String result = null;
         if ( isRelayGame() ) {
             
            if ( CommsAddrRec.CommsConnType.COMMS_CONN_RELAY == conType ) {
                int fmtID;
                if ( null == relayID || 0 == relayID.length() ) {
                    fmtID = R.string.summary_relay_conff;
                } else if ( anyMissing() ) {
                    fmtID = R.string.summary_relay_waitf;
                } else if ( gameOver ) {
                    fmtID = R.string.summary_relay_gameoverf;
                } else {
                    fmtID = R.string.summary_relay_connf;
                }
                result = String.format( m_context.getString(fmtID), roomName );
             }
         }
         return result;
     }
 
     public boolean isRelayGame()
     {
         // This definition will expand as other transports are added
         return ( null != conType 
                  && serverRole != CurGameInfo.DeviceRole.SERVER_STANDALONE );
     }
 
     private boolean isLocal( int indx ) {
         int flag = 2 << (indx * 2);
         return 0 == (m_giFlags & flag);
     }
 
     private boolean isRobot( int indx ) {
         int flag = 1 << (indx * 2);
         boolean result = 0 != (m_giFlags & flag);
         return result;
     }
 
     private boolean anyMissing()
     {
         boolean missing = false;
         for ( int ii = 0; ii < nPlayers; ++ii ) {
             if ( !isLocal(ii) && (0 != ((1 << ii) & missingPlayers) ) ) {
                 missing = true;
                 break;
             }
         }
         return missing;
     }
 
     public int giflags() {
         int result;
         if ( null == m_gi ) {
             result = m_giFlags;
         } else {
             result = 0;
             for ( int ii = 0; ii < m_gi.nPlayers; ++ii ) {
                 if ( ! m_gi.players[ii].isLocal ) {
                     result |= 2 << (ii * 2);
                 }
                 if ( m_gi.players[ii].isRobot() ) {
                     result |= 1 << (ii * 2);
                 }
             }
         }
         return result;
     }
 
     public void setGiFlags( int flags ) 
     {
         m_giFlags = flags;
     }
 
     public String summarizePlayer( int indx ) 
     {
         String player = players[indx];
         int formatID = 0;
         if ( !isLocal(indx) ) {
             boolean isMissing = 0 != ((1 << indx) & missingPlayers);
             if ( isMissing ) {
                 player = m_context.getString( R.string.missing_player );
             } else {
                 formatID = R.string.str_nonlocal_namef;
             }
         } else if ( isRobot(indx) ) {
             formatID = R.string.robot_namef;
         }
 
         if ( 0 != formatID ) {
             String format = m_context.getString( formatID );
             player = String.format( format, player );
         }
         return player;
     }
 
     public String playerNames()
     {
         String[] names = null;
         if ( null != m_gi ) {
             names = m_gi.visibleNames( false );
         } else if ( null != m_playersSummary ) {
             names = TextUtils.split( m_playersSummary, "\n" );
         }
 
         String result = null;
         if ( null != names && 0 < names.length ) {
             String joiner = m_context.getString( R.string.vs_join );
             result = TextUtils.join( joiner, names );
         }
 
         return result;
     }
 
     public boolean isNextToPlay( int indx ) {
         return indx == turn && isLocal(indx);
     }
 
     public String dictNames( String separator ) 
     {
         String list = null;
         if ( null != m_gi ) {
             String[] names = m_gi.dictNames();
             list = TextUtils.join( separator, names );
         }
         return String.format( "%s%s%s", separator, list, separator );
     }
 
 }
