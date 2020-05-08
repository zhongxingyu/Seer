 /*******************************************************************************
  * Copyright (c) 2012 sfleury.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     sfleury - initial API and implementation
  ******************************************************************************/
 package org.gots.action;
 
 import org.gots.action.bean.BeakeringAction;
 import org.gots.action.bean.CuttingAction;
 import org.gots.action.bean.HarvestAction;
 import org.gots.action.bean.HoeAction;
 import org.gots.action.bean.LighteningAction;
 import org.gots.action.bean.SowingAction;
 import org.gots.action.bean.WateringAction;
 
 import android.content.Context;
 
 public class ActionFactory {
 	public ActionFactory() {
 		// TODO Auto-generated constructor stub
 	}
 
 	public BaseActionInterface buildAction(Context context, String name) {
 		BaseActionInterface seedAction = null;
 		if ("beak".equals(name))
 			seedAction = new BeakeringAction(context);
 		else if ("cut".equals(name))
 			seedAction = new CuttingAction(context);
 		else if ("lighten".equals(name))
 			seedAction = new LighteningAction(context);
 		
 		else if ("water".equals(name))
 			seedAction = new WateringAction(context);
 		else if ("sow".equals(name))
 			seedAction = new SowingAction(context);
 		else if ("hoe".equals(name))
 			seedAction = new HoeAction(context);
 		else if ("harvest".equals(name))
 			seedAction = new HarvestAction(context);
		else seedAction = new SowingAction(context);
 		return seedAction;
 
 	}
 
 }
