 /*
  *  Copyright 2009 Mathieu ANCELIN.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */

 package cx.ath.mancel01.dependencyshot.test;
 
 import cx.ath.mancel01.dependencyshot.graph.Binder;
 import javax.inject.Inject;
 import javax.inject.Provider;
 import org.atinject.tck.auto.Car;
 import org.atinject.tck.auto.Convertible;
 import org.atinject.tck.auto.Drivers;
 import org.atinject.tck.auto.DriversSeat;
 import org.atinject.tck.auto.Engine;
 import org.atinject.tck.auto.Seat;
 import org.atinject.tck.auto.Tire;
 import org.atinject.tck.auto.V8Engine;
 import org.atinject.tck.auto.accessories.SpareTire;
 
 /**
  *
  * @author Mathieu ANCELIN
  */
 public class TCKFluentBinder extends Binder {
 
    @Override 
     public void configureBindings() {
         bind(Car.class).to(Convertible.class);
         bind(Seat.class).annotatedWith(Drivers.class).to(DriversSeat.class);
         bind(Engine.class).to(V8Engine.class);
         bind(Tire.class).named("spare").providedBy(new Provider<Tire>() {
 
             @Inject
             private SpareTire tire;
 
  			@Override
  			public Tire get() {
                 return (Tire) tire;
             }
  		});
     }
 }
