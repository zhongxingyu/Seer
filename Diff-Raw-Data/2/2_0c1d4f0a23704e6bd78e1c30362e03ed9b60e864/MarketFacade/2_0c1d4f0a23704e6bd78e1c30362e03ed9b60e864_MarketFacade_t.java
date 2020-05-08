 /*
  * investovator, Stock Market Gaming framework
  * Copyright (C) 2013  investovator
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.investovator.jasa.api;
 
 import net.sourceforge.jabm.event.EventListener;
 import net.sourceforge.jabm.event.SimEvent;
 import net.sourceforge.jabm.report.Report;
import org.investovator.core.commons.simulationengine.SimulationFacade;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * @author rajith
  * @version $Revision$
  */
 public interface MarketFacade extends SimulationFacade {
 
     /**
      *
      * @return {@link Report}
      */
     public HashMap<String, ArrayList<Report>> getReports();
 
     public void addListener(String stockID, EventListener eventListener);
 }
