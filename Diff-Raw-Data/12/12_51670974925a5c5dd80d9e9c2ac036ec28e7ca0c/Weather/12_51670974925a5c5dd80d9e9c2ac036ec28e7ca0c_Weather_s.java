 /**
  * Copyright 2013 iFactory Ltd.
  * 
  * Min Kim (minsikzzang@gmail.com)
  *
  * The Netty Project licenses this file to you under the Apache License,
  * version 2.0 (the "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at:
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.ifactory.client.greenwich;
 
 import java.io.Serializable;
 
 public class Weather implements Serializable {
 	
 	private long id;
 	private String keyword;
 	private String description;
 	private String icon;
 	
 	static final class Builder {
 	/**
	Thunderstorm
     ID	Meaning	Icon
     200	 thunderstorm with light rain	 [[file:11d.png]]
     201	 thunderstorm with rain	 [[file:11d.png]]
     202	 thunderstorm with heavy rain	 [[file:11d.png]]
     210	 light thunderstorm	 [[file:11d.png]]
     211	 thunderstorm	 [[file:11d.png]]
     212	 heavy thunderstorm	 [[file:11d.png]]
     221	 ragged thunderstorm	 [[file:11d.png]]
     230	 thunderstorm with light drizzle	 [[file:11d.png]]
     231	 thunderstorm with drizzle	 [[file:11d.png]]
     232	 thunderstorm with heavy drizzle	 [[file:11d.png]]
     Drizzle
     ID	 Meaning	Icon
     300	 light intensity drizzle	 [[file:09d.png]]
     301	 drizzle	 [[file:09d.png]]
     302	 heavy intensity drizzle	 [[file:09d.png]]
     310	 light intensity drizzle rain	 [[file:09d.png]]
     311	 drizzle rain	 [[file:09d.png]]
     312	 heavy intensity drizzle rain	 [[file:09d.png]]
     321	 shower drizzle	 [[file:09d.png]]
     Rain
     ID	 Meaning	Icon
     500	 light rain	 [[file:10d.png]]
     501	 moderate rain	 [[file:10d.png]]
     502	 heavy intensity rain	 [[file:10d.png]]
     503	 very heavy rain	 [[file:10d.png]]
     504	 extreme rain	 [[file:10d.png]]
     511	 freezing rain	 [[file:13d.png]]
     520	 light intensity shower rain	 [[file:09d.png]]
     521	 shower rain	 [[file:09d.png]]
     522	 heavy intensity shower rain	 [[file:09d.png]]
     Snow
     ID	 Meaning	Icon
     600	 light snow	 [[file:13d.png]]
     601	 snow	 [[file:13d.png]]
     602	 heavy snow	 [[file:13d.png]]
     611	 sleet	 [[file:13d.png]]
     621	 shower snow	 [[file:13d.png]]
     Atmosphere
     ID	 Meaning	Icon
     701	 mist	 [[file:50d.png]]
     711	 smoke	 [[file:50d.png]]
     721	 haze	 [[file:50d.png]]
     731	 Sand/Dust Whirls	 [[file:50d.png]]
     741	 Fog	 [[file:50d.png]]
     Clouds
     ID	 Meaning	Icon
     800	 sky is clear	 [[file:01d.png]] [[file:01n.png]]
     801	 few clouds	 [[file:02d.png]] [[file:02n.png]]
     802	 scattered clouds	 [[file:03d.png]] [[file:03d.png]]
     803	 broken clouds	 [[file:04d.png]] [[file:03d.png]]
     804	 overcast clouds	 [[file:04d.png]] [[file:04d.png]]
     Extreme
     ID	 Meaning
     900	 tornado
     901	 tropical storm
     902	 hurricane
     903	 cold
     904	 hot
     905	 windy
     906	 hail    
 	*/
 		private long id;
 		private String keyword;
 		private String description;
 		private String icon;
 		
 		public Builder(long id, String keyword) {
 			this.id = id;
 			this.keyword = keyword;				
 		}
 
 		public Builder description(String description) {
 			this.description = description;
 			return this;
 		}	
 		
 		public Builder icon(String icon) {
 			this.icon = icon;
 			return this;
 		}			
 		
 		public Weather build() {
 			return new Weather(this);
 		}
 	}
 	
 	private Weather(Builder builder) {
 		id = builder.id;
 		keyword = builder.keyword;
 		description = builder.description;
 		icon = builder.icon;			
 	}	
 	
 	public String getKeyword() {
 		return keyword;
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 	
 	public String getIcon() {
 		return icon;
 	}
 	
 	public long getId() {
 		return id;
 	}
 }
