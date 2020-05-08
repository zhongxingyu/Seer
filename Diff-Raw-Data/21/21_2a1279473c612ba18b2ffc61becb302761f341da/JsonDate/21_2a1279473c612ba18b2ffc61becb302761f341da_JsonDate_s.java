 /*
  * Copyright 2010 kk-electronic a/s. 
  * 
  * This file is part of KKPortal.
  *
  * KKPortal is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * KKPortal is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with KKPortal.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.kk_electronic.kkportal.core.rpc.jsonformat;
 
 import java.util.Date;
 import java.util.List;
 
 import com.google.gwt.json.client.JSONNumber;
 import com.google.gwt.json.client.JSONValue;
 import com.kk_electronic.kkportal.core.rpc.FrameEncoder;
import com.kk_electronic.kkportal.core.rpc.SimpleEncoder;
 
 /**
 * @author albatros
  *
  */
 public class JsonDate implements JsonValue<Date> {
 
 	@Override
 	public Date fromJson(JSONValue jsonValue, List<Class<?>> subtypes,
			SimpleEncoder simpleEncoder) throws UnableToDeserialize {
 		if (jsonValue.isNull() != null)
 			return null;
 		if (jsonValue.isNumber() == null)
 			throw new UnableToDeserialize("Expected Json Number");
 		JSONNumber o = jsonValue.isNumber();
 		return new Date((long)(o.doubleValue()));
 	}
 
 	/* (non-Javadoc)
 	 * @see com.kk_electronic.kkportal.core.rpc.jsonformat.JsonValue#toJson(java.lang.StringBuilder, java.lang.Object, com.kk_electronic.kkportal.core.rpc.FrameEncoder)
 	 */
 	@Override
 	public void toJson(StringBuilder response, Date object,
 			FrameEncoder<JSONValue> encoder) throws UnableToSerialize {
 		response.append(object.getTime());
 	}
 }
