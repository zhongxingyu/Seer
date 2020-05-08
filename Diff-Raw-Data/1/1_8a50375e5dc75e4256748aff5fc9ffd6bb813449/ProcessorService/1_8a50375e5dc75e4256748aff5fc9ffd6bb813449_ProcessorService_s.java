 /*
  * JBoss, Home of Professional Open Source Copyright 2011 Red Hat Inc. and/or
  * its affiliates and other contributors as indicated by the @authors tag. All
  * rights reserved. See the copyright.txt in the distribution for a full listing
  * of individual contributors.
  * 
  * This copyrighted material is made available to anyone wishing to use, modify,
  * copy, or redistribute it subject to the terms and conditions of the GNU
  * Lesser General Public License, v. 2.1.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
  * USA.
  */
 package org.jboss.ircbot.services;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.jboss.ircbot.BotConfig;
 import org.jboss.ircbot.BotService;
 import org.jboss.ircbot.config.Channel;
 import org.jboss.ircbot.config.Channels;
 import org.jboss.ircbot.config.Server;
 import org.jboss.ircbot.impl.BotConfigImpl;
 import org.jboss.ircbot.impl.BotRuntimeImpl;
 import org.jboss.logging.Logger;
 import org.jboss.msc.inject.Injector;
 import org.jboss.msc.service.Service;
 import org.jboss.msc.service.StartContext;
 import org.jboss.msc.service.StartException;
 import org.jboss.msc.service.StopContext;
 import org.jboss.msc.value.InjectedValue;
 import org.w3c.dom.Element;
 import org.x2jb.bind.XML2Java;
 
 /**
  * @author <a href="ropalka@redhat.com">Richard Opalka</a>
  */
 public final class ProcessorService implements Service< Void > {
 
     private static final Logger LOG = Logger.getLogger( ProcessorService.class );
     private final BotService< ? > producer;
     private final InjectedValue< Server > injectedConfig = new InjectedValue< Server >();
     private BotConfig config;
     private Object pluginConfig;
     private final Element element;
 
     ProcessorService( final BotService< ? > producer, final Element element ) {
         this.producer = producer;
         this.element = element;
     }
 
     Injector< Server > getInjector() {
         return injectedConfig;
     }
 
     private void init() {
         config = toBotConfig( injectedConfig.getValue() );
         final Class< ? > configClass = producer.getConfigClass();
         if ( configClass != null ) {
             pluginConfig = XML2Java.bind( element, configClass );
         }
     }
 
     public Void getValue() {
         return null;
     }
 
     public void start( final StartContext context ) throws StartException {
         try {
             init();
             producer.init( new BotRuntimeImpl( config, pluginConfig, producer ) );
         }
         catch ( final Exception e ) {
             LOG.error( e.getMessage(), e );
             context.failed( new StartException( e ) );
         }
     }
 
     private static BotConfig toBotConfig( final Server cfg ) {
         final String serverAddress = cfg.getAddress();
         final int serverPort = cfg.getPort();
         final Set< String > serverChannels = toStringSet( cfg.getChannels() );
         final String botNick = cfg.getBotName();
         final String botFullName = cfg.getBotFullName();
         final String botPassword = cfg.getBotPassword();
         return new BotConfigImpl( serverAddress, serverPort, serverChannels, botNick, botFullName, botPassword );
     }
 
     private static Set< String > toStringSet( final Channels channels ) {
         final Set< String > retVal = new HashSet< String >();
         for ( final Channel channel : channels.getArray() ) {
             retVal.add( channel.getName() );
         }
         return Collections.unmodifiableSet( retVal );
     }
 
     public void stop( final StopContext context ) {
         try {
             producer.destroy();
         }
         catch ( final Exception e ) {
             LOG.error( e.getMessage(), e );
         }
     }
 }
