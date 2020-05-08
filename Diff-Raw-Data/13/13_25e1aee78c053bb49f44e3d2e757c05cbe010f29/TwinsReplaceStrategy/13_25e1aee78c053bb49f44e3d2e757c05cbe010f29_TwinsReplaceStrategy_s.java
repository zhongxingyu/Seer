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
 
 package pgrid.service.repair.internal;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import pgrid.entity.PGridPath;
 import pgrid.entity.routingtable.RoutingTable;
 import pgrid.service.anotations.constants.MaxRef;
 import pgrid.service.repair.spi.ReplaceStrategy;
 import pgrid.utilities.ArgumentCheck;
 
import javax.inject.Inject;

 /**
  * <h1>The implementation of the Thesis replace algorithm.</h1>
  * <p/>
  * If the failed host is the conjugate of the localhost, then it will reduce
  * its path to the common prefix of both.
  * <br/>
  * <b><i>Example:</i></b> If the failed host is on "1" and the localhost on
  * "0", then simply the localhost will move to path "". That is, it will hold
  * the whole keyspace. The example is analogous to longer paths.
  * <p/>
  * If the conjugate has nothing to do with the failed host, then the host with
  * path ending with '1' will take the path of the failed and the host with path
  * ending with '0' will reduce it path by one.
  * <br/>
  * <b><i>Example:</i></b> If the failed host is on "1" and the localhost is on
  * "00" and its conjugate on "01", then the new path of the localhost will be
  * "0" and that of its conjugate will be "1". The example is analogous to
  * longer paths.
  *
  * @author Vourlakis Nikolas <nvourlakis@gmail.com>
  */
 public class TwinsReplaceStrategy implements ReplaceStrategy {
 
     private final int maxRef_;
 
     @Inject
     public TwinsReplaceStrategy(@MaxRef int maxRef) {
         maxRef_ = maxRef;
     }
 
     private static final Logger logger_ = LoggerFactory.getLogger(TwinsReplaceStrategy.class);
 
 
     @Override
     public void execute(RoutingTable routingTable, PGridPath failedPath) {
         ArgumentCheck.checkNotNull(routingTable, "Null routing table was given.");
         ArgumentCheck.checkNotNull(failedPath, "Null path was given");
 
         PGridPath localhostPath = routingTable.getLocalhost().getHostPath();
 
         if (failedPath.isConjugateTo(localhostPath)) {
             logger_.info("[Failed = conjugate] Localhost will reduce its path by one.");
             routingTable.getLocalhost().setHostPath(reducePath(localhostPath));
         } else {
             if (localhostPath.value(localhostPath.length() - 1) == '1') {
                 logger_.info("[Ending with '1'] Localhost will take the path of the failed host.");
                 routingTable.getLocalhost().setHostPath(failedPath.toString());
             } else {
                 logger_.info("[Ending with '0'] Localhost will reduce its path by one.");
                 routingTable.getLocalhost().setHostPath(reducePath(localhostPath));
             }
         }
         logger_.debug("Localhost new path: {}", routingTable.getLocalhost().getHostPath());
         routingTable.refresh(maxRef_);
     }
 
     private String reducePath(PGridPath localhostPath) {
         int end = localhostPath.length() - 1;
         return end < 0 ? "" : localhostPath.subPath(0, end);
     }
 }
