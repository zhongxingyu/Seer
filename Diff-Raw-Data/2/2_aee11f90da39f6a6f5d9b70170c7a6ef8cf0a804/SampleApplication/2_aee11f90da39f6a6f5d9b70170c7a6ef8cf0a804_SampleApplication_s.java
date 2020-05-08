 /*
  * Copyright 2011 Szabolcs Berecz
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package domper.config.sample;
 
 import static java.lang.String.format;
 
 import java.io.Serializable;
 
 import android.app.Application;
 import android.util.Log;
 import domper.config.ConfigStore;
 import domper.config.ConfigStore.OnConfigChangedListener;
 
 public final class SampleApplication extends Application {
 
	protected static final String TAG = "TestApplication";
 
 	static class Config implements Serializable {
 		public int n = 1;
 		public long m = 0xfffffffffL;
 		public String s = "beuwge";
 		public boolean b = true;
 		public boolean b2 = false;
 		public double d = 3.14;
 		public Object o = null;
 		public double d2 = 3.14;
 		public Object o2 = null;
 	}
 
 	private ConfigStore<Config> configStore;
 
 	@Override
 	public void onCreate() {
 		configStore = ConfigStore.createInstance(this, new Config(), new OnConfigChangedListener<Config>() {
 			@Override
 			public void onConfigChanged(Config newConfig) {
 				Log.d(TAG, format("Config changed: %s", newConfig));
 			}
 		});
 	}
 }
