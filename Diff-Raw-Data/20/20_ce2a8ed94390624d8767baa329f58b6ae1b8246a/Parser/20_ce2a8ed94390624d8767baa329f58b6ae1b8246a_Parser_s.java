 /*
  * © Copyright 2008–2010 by Edgar Kalkowski <eMail@edgar-kalkowski.de>
  * 
  * This file is part of the chatbot xpeter.
  * 
  * The chatbot xpeter is free software; you can redistribute it and/or modify it under the terms of
  * the GNU General Public License as published by the Free Software Foundation; either version 3 of
  * the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program. If
  * not, see <http://www.gnu.org/licenses/>.
  */
 
 package erki.xpeter.parsers;
 
 import erki.xpeter.Bot;
 
 /**
  * Implemented by all classes that want to parse messages received from the chats the bot is
  * connected to. If you want to add functionality to the bot you want to implement this one.
  * <p>
  * It is strongly recommended that every parser has a unique {@link Class#getSimpleName()} although
  * this is not an absolute requirement (see how {@link ParserLoader} works to understand the
  * details).
  * 
  * @author Edgar Kalkowski
  */
 public interface Parser {
     
     /**
      * As the parser classes are instanciated using reflection (and thus no constructor can be used)
      * this method is used for initializing this parser and associating it to the instance of
      * {@link Bot} it belongs to. Through this Bot instance the parsers can access all connections
      * the bot is currently connected to. This is useful to interconnect channels on different
      * servers with the bot.
      * <p>
      * <b>The parser also must register itself with the bot instance to receive notifications if
      * certain types of message are received.</b>
      * 
      * @param bot
      *        The instance of {@link Bot} this parser object belongs to and through which e.g. all
      *        the connections to the different chats may be accessed.
      */
     public void init(Bot bot);
     
     /**
      * This is called if a parser is unloaded. The parser shall deregister all previously registered
      * listeners and exit all it’s threads.
      * 
      * @param bot
      *        The instance of {@link Bot} this parser object belongs to.
      */
     public void destroy(Bot bot);
 }
