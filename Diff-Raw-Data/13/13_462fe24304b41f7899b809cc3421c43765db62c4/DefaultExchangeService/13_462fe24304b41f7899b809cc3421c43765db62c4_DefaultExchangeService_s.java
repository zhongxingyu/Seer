 /*
  * This file is part of the pgrid project.
  *
  * Copyright (c) 2012. Vourlakis Nikolas. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package pgrid.service.exchange.internal;
 
 import org.omg.CORBA.ORB;
 import org.omg.CORBA.SystemException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import pgrid.entity.Host;
 import pgrid.entity.routingtable.RoutingTable;
 import pgrid.service.CommunicationException;
 import pgrid.service.corba.exchange.ExchangeHandle;
 import pgrid.service.corba.exchange.ExchangeHandleHelper;
 import pgrid.service.exchange.ExchangeService;
 import pgrid.service.exchange.spi.ExchangeAlgorithm;
 import pgrid.service.exchange.spi.ExchangeContext;
 import pgrid.utilities.ArgumentCheck;
 import pgrid.utilities.Deserializer;
 import pgrid.utilities.Serializer;
 
 import java.util.Collection;
 
 /**
  * @author Vourlakis Nikolas
  */
 public class DefaultExchangeService implements ExchangeService {
     private static final Logger logger_ = LoggerFactory.getLogger(DefaultExchangeService.class);
     private final RoutingTable routingTable_;
     private final ORB orb_;
     private ExchangeAlgorithm algorithm_;
     private int recursions_;
 
     private final int MAX_RECURSIONS;
     private final int MAX_REF;
 
     public DefaultExchangeService(ORB orb, RoutingTable routingTable, ExchangeAlgorithm algorithm, int maxRef, int maxRecur) {
         ArgumentCheck.checkNotNull(orb, "Cannot initialize a DefaultExchangeService object with a null ORB value.");
         ArgumentCheck.checkNotNull(routingTable, "Cannot initialize a DefaultExchangeService object with a null RoutingTable value.");
         ArgumentCheck.checkNotNull(algorithm, "Cannot initialize a DefaultExchangeService object with a null ExchangeAlgorithm value.");
 
         orb_ = orb;
         routingTable_ = routingTable;
         algorithm_ = algorithm;
         recursions_ = 0;
         MAX_RECURSIONS = maxRecur;
         MAX_REF = maxRef;
     }
 
     @Override
     public void execute(Host host) throws CommunicationException {
         if (host.compareTo(routingTable_.getLocalhost()) == 0) {
             logger_.info("Exchange service stopped because the given host was the local host.");
             return;
         }
         logger_.info("Executing exchange service.");
 
         ExchangeHandle handle = getRemoteHandle(host);
         // send local routing table
         handle.exchange(Serializer.serializeRoutingTable(routingTable_));
         // receive remote routing table
         RoutingTable remoteRT = Deserializer.deserializeRoutingTable(handle.routingTable());
 
         ExchangeContext context = new ExchangeContext(routingTable_, false, MAX_REF);
         context.setRemoteInfo(remoteRT);
 
         logger_.debug("Local peer has all the information needed to execute the exchange algorithm.");
         algorithm_.execute(context);
 
         if (context.isRecursive() && recursions_ < MAX_RECURSIONS) {
             logger_.debug("Executing recursion: {}", recursions_);
             ++recursions_;
             Collection<Host> level = context.getRemoteRoutingTable().getLevel(context.getCommonPathLength());
             if (level == null || level.isEmpty()) {
                 return;
             }
             for (Host toExchange : level) {
                execute(toExchange);
             }
         }
     }
 
     public ExchangeHandle getRemoteHandle(Host host) throws CommunicationException {
         String[] exchangeHandleID = ExchangeHandleHelper.id().split(":");
         String corbaloc = "corbaloc:iiop:[" +
                 host.getAddress().getHostAddress() + "]:" + host.getPort()
                 + "/" + exchangeHandleID[1];
         logger_.debug("CORBALOC: {}", corbaloc);
         org.omg.CORBA.Object object = orb_.string_to_object(corbaloc);
 
         ExchangeHandle handle;
         try {
             handle = ExchangeHandleHelper.narrow(object);
         } catch (SystemException e) {
             logger_.warn("Cannot reach the host {}:{} that was asked to exchange with.", host, host.getPort());
             throw new CommunicationException(e.getCause());
         }
         return handle;
     }
 }
