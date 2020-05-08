 /*
 * Copyright (C) 2011 The Liquid Settings Project
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
 
 package com.liquid.settings.lists;
 
 import com.liquid.settings.R;
 
 import java.util.ArrayList;
 
 public class ExtrasList extends MasterLists {
     public static ArrayList<MasterLists.List> mList = null;
 
     public ExtrasList() {
         mList = new ArrayList<MasterLists.List>();
         mList.add(new MasterLists.List(R.string.propmodder_title, R.string.propmodder_summary, "com.liquid.settings.externals.PropModderActivity", TYPE_NORMAL));
        mList.add(new MasterLists.List(R.string.parchment_title, R.string.parchment_summary, "com.liquid.settings.externals.SplashScreen", TYPE_NORMAL));
     }
 
     public ArrayList<MasterLists.List> getList() {
         return mList;
     }
 }
