 /* 
   Copyright 2010 Duncan Mac-Vicar P. <dmacvicar@suse.de>
  
   This file is part of the openSUSE Build Service Android Client.
 
   openSUSE Build Service Android Client is free software: you
   can  redistribute it and/or modify it under the terms of the GNU
   General Public License as published by the Free Software Foundation,
   either version 3 of the License, or (at your option) any later
   version.
 
   openSUSE Build Service Android Client is distributed in the hope
   that it will be useful, but WITHOUT ANY WARRANTY; without even the
   implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   See the GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
   
 */
 
 package org.opensuse.android;
 
import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.opensuse.android.obs.data.Request;
 import org.simpleframework.xml.transform.Matcher;
 import org.simpleframework.xml.transform.Transform;
 
import android.util.Log;

 /*
  * matcher for the serializer framework that supports our custom
  * types
  * 
  * @author Duncan Mac-Vicar P. <dmacvicar@suse.de>
  */
 public class TypesMatcher implements Matcher {
 	@Override
 	public Transform match(Class type) throws Exception {
        if(type == Date.class) {
           return new DateTransform();
        }
        else if (type == Request.Action.Type.class) {
     	   return new RequestActionTypeTransform();
        }
        return null;
     }
 }
