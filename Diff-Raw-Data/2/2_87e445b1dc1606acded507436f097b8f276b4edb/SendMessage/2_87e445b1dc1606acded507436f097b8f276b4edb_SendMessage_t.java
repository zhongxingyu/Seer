 package ca.szc.keratin.core.net.message;
 
 /**
  * Assists in sending standard IrcMessage instances by easing their creation.
  * 
  * @see IrcMessage
  */
 public class SendMessage
 {
     // TODO move these protected defs to a superclass, then have a ReceiveMessage class with static methods as well?
 
     public static class BadStandardValues
         extends RuntimeException
     {
         private static final long serialVersionUID = -5190231809043802158L;
 
         public BadStandardValues( Throwable cause )
         {
             super( "The constants in this class should always be accepted, but they have been rejected", cause );
         }
     }
 
     protected static final String INVITE_COMMAND = "INVITE";
 
     protected static final String JOIN_COMMAND = "JOIN";
 
     protected static final String KICK_COMMAND = "KICK";
 
     protected static final String MODE_COMMAND = "MODE";
 
     protected static final String NAMES_COMMAND = "NAMES";
 
    protected static final String NICK_COMMAND = "NICK";
 
     protected static final String NOTICE_COMMAND = "NOTICE";
 
     protected static final String PART_COMMAND = "PART";
 
     protected static final String PING_COMMAND = "PING";
 
     protected static final String PONG_COMMAND = "PONG";
 
     protected static final String PRIVMSG_COMMAND = "PRIVMSG";
 
     protected static final String QUIT_COMMAND = "QUIT";
 
     protected static final String TOPIC_COMMAND = "TOPIC";
 
     protected static final String USER_COMMAND = "USER";
 
     /**
      * Creates an <a href="https://tools.ietf.org/html/rfc1459.html#section-4.2.7">INVITE</a> message
      * 
      * @param nick The nick to target
      * @param channel The channel to target. Must start with a # character.
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage invite( String nick, String channel )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, INVITE_COMMAND, nick, channel );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.2.1">JOIN</a> message
      * 
      * @param channel The channel to target. Must start with a # character.
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage join( String channel )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, JOIN_COMMAND, channel );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.2.1">JOIN</a> message
      * 
      * @param channel The channel to target. Must start with a # character.
      * @param key The key of the channel.
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage join( String channel, String key )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, JOIN_COMMAND, channel, key );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.2.8">KICK</a> message
      * 
      * @param channel The channel to target. Must start with a # character.
      * @param nick The nick of the user to target
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage kick( String channel, String nick )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, KICK_COMMAND, channel, nick );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.2.8">KICK</a> message
      * 
      * @param channel The channel to target. Must start with a # character.
      * @param nick The nick of the user to target
      * @param comment The reason for this action
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage kick( String channel, String nick, String comment )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, KICK_COMMAND, channel, nick, comment );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.2.3">MODE</a> message
      * 
      * @param target The thing to target. Channels must start with a # character.
      * @param mode The mode flags to apply
      * @param params Any additional parameters. Only applicable to Channel modes.
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage mode( String target, String mode, String... params )
         throws InvalidMessageParamException
     {
         try
         {
             if ( params != null && params.length > 0 )
             {
                 // Combine the given parameters with our varargs to satisfy the IrcMessage varargs
                 final int nonVarargParams = 2;
                 String[] parameters = new String[params.length + nonVarargParams];
                 parameters[0] = target;
                 parameters[1] = mode;
                 for ( int i = nonVarargParams; i < params.length; i++ )
                     parameters[i] = params[i - nonVarargParams];
 
                 return new IrcMessage( null, MODE_COMMAND, parameters );
             }
             else
             {
                 return new IrcMessage( null, MODE_COMMAND, target, mode );
             }
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.2.5">NAMES</a> message
      * 
      * @param channel The channel to query. Must start with a # character.
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage names( String channel )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, NAMES_COMMAND, channel );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.1.2">NICK</a> message
      * 
      * @param nick The new nick
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage nick( String nick )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, NICK_COMMAND, nick );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.4.2">NOTICE</a> message
      * 
      * @param target The thing to target. Channels must start with a # character.
      * @param text The text to include
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage notice( String target, String text )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, NOTICE_COMMAND, target, text );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.2.2">PART</a> message
      * 
      * @param channel The channel to target. Must start with a # character.
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage part( String channel )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, PART_COMMAND, channel );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.6.2">PING</a> message
      * 
      * @param server1 server which sent the PING message out (de facto, doesn't matter what the content is, it will be
      *            echoed regardless).
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage ping( String server1 )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, PING_COMMAND, server1 );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.6.2">PING</a> message
      * 
      * @param server1 server which sent the PING message out
      * @param server2 target of the ping, the message gets forwarded there.
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage ping( String server1, String server2 )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, PING_COMMAND, server1, server2 );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.6.3">PONG</a> message
      * 
      * @param daemon1 the name of the daemon who responds to the PING message
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage pong( String daemon1 )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, PONG_COMMAND, daemon1 );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.6.3">PONG</a> message
      * 
      * @param daemon1 the name of the daemon who responds to the PING message
      * @param daemon2 target of the pong, the message gets forwarded there.
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage pong( String daemon1, String daemon2 )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, PONG_COMMAND, daemon1, daemon2 );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.4.1">PRIVMSG</a> message
      * 
      * @param target The thing to target. Channels must start with a # character.
      * @param text The text to include
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage privmsg( String target, String text )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, PRIVMSG_COMMAND, target, text );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.1.6">QUIT</a> message
      * 
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage quit()
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, QUIT_COMMAND );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.1.6">QUIT</a> message
      * 
      * @param comment The reason for this action
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage quit( String comment )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, QUIT_COMMAND, comment );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.2.4">TOPIC</a> message
      * 
      * @param channel The channel to query. Must start with a # character.
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage topic( String channel )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, TOPIC_COMMAND, channel );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc1459.html#section-4.2.4">TOPIC</a> message
      * 
      * @param channel The channel to target. Must start with a # character.
      * @param topic The new topic text
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage topic( String channel, String topic )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, TOPIC_COMMAND, channel, topic );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 
     /**
      * Creates a <a href="https://tools.ietf.org/html/rfc2812.html#section-3.1.3">USER</a> message
      * 
      * @param user The user name
      * @param mode a numeric mode value
      * @param realName The full name of the user
      * @return An IrcMessage of the specified type with the given parameters
      * @throws InvalidMessageParamException If one of the given parameters fails validation
      */
     public static IrcMessage user( String user, String mode, String realName )
         throws InvalidMessageParamException
     {
         try
         {
             return new IrcMessage( null, USER_COMMAND, user, mode, "*", realName );
         }
         catch ( InvalidMessagePrefixException | InvalidMessageCommandException e )
         {
             throw new BadStandardValues( e );
         }
     }
 }
