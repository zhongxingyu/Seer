 /*******************************************************************************
  * Copyright 2013 Yoann Mikami <yoann@ymkm.org>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 
 package org.ymkm.lib.controller.support;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ymkm.lib.controller.core.ControllableActivity;
 
 import android.app.Application;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 
 /**
  * ControlledActivity automates the process of registering/unregistering
  * to the FragmentControllerApplication.
  * 
  * It also implements a default sane value for the controllable name.<br>
  * Default is class name + hash code, to ensure unicity of its name.<br>
  * Doing so makes it difficult to use {@linkplain FragmentControllerApplication#getFragmentFor(String, int)}
  * or {@linkplain FragmentControllerApplication#getMessengerFor(String, int)}, as it requires
  * a valid controllable name as its first parameter.<br>
  * 
  * Therefore, subclasses may wish to override the value for more control.
  * 
  * @author yoann@ymkm.org
  */
 public abstract class ControlledActivity extends FragmentActivity implements ControllableActivity<FragmentManager,FragmentTransaction> {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Application app = getApplication();
 		assert(app instanceof FragmentControllerApplication);
 		_controller = new WeakReference<FragmentController>(
 				((FragmentControllerApplication) app).getController());
 	}
 
 
 	/**
 	 * Registers this activity on start
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		getController().register(this);
 	}
 
 	/**
 	 * Unregisters this activity on stop
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		getController().unregister(this);
 	}
 
 	@Override
 	public final FragmentController getController() {
 		return this._controller.get();
 	}
 
 	/**
 	 * Default implementation is to return null, aka no callbacks
 	 * 
 	 * @param boolean dummy parameter
 	 */
 	@Override
 	public final List<Class<? extends org.ymkm.lib.controller.core.FragmentControllerCallbackAbstract<FragmentManager,FragmentTransaction>>> getCallbacks(boolean b) {
 		List<Class<? extends org.ymkm.lib.controller.core.FragmentControllerCallbackAbstract<FragmentManager,FragmentTransaction>>> list = new
 				ArrayList<Class<? extends org.ymkm.lib.controller.core.FragmentControllerCallbackAbstract<FragmentManager,FragmentTransaction>>>();
 		List<Class<? extends FragmentControllerCallback>> l = getCallbacks();
 		if (null != l && l.size()>0) {
 			for (Class<? extends FragmentControllerCallback> c : l) {
 				list.add((Class<? extends org.ymkm.lib.controller.core.FragmentControllerCallbackAbstract<FragmentManager,FragmentTransaction>>) c);
 			}
 		}
 		return list;
 	}
 
 	public final void runDelayedOnUiThread(final Runnable runnable, long delayMillis) {
 		getController().postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				runOnUiThread(runnable);
 			}
 		}, delayMillis);
 	}
 	
 	/**
	 * Default implementation returns the canonical class name + instance hash code
 	 */
 	@Override
 	public String getControllableName() {
		return String.valueOf(this.getClass().getCanonicalName() + this.hashCode());
 	}
 
 	/**
 	 * Default implementation is to return null, aka no callbacks
 	 */
 	public List<Class<? extends FragmentControllerCallback>> getCallbacks() {
 		return null;
 	}
 
 
 	private WeakReference<FragmentController> _controller;
 }
