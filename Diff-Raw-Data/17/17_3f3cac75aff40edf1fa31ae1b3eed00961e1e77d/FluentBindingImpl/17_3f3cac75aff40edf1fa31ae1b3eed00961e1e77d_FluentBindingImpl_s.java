 /**
  * Copyright (C) 2013
  * Nicholas J. Little <arealityfarbetween@googlemail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package little.nj.gui.binding;
 
 import little.nj.gui.binding.events.BindingEvent;
 import little.nj.gui.binding.events.BindingEventSource;
 import little.nj.gui.binding.events.BindingListener;
 
 
 public class FluentBindingImpl<X, Y>
     extends GenericBindingImpl<X, Y>
     implements FluentBinding<X, Y> {
 
     public final static <X, Y> FluentBindingImpl<X, Y> bind(Class<X> sample1,
                                                             Class<Y> sample2)
     {
         return new FluentBindingImpl<X, Y>();
     }
 
     protected BindingEventSource events;
     protected FluentBinding<Y, X> twin;
 
     public FluentBindingImpl() { }
 
     public FluentBindingImpl(Class<X> sample1, Class<Y> sample2) { }
 
     public FluentBindingImpl<X, Y> from(Getter<? extends X> get) {
         this.get = get;
         return this;
     }
 
     public FluentBindingImpl<X, Y> to(Setter<? super Y> set) {
         this.set = set;
         return this;
     }
 
     public FluentBindingImpl<X, Y> via(Marshal<? super X, ? extends Y> marshal) {
         this.marshal = marshal;
         return this;
     }
 
     public FluentBindingImpl<X, Y> when(BindingEventSource events) {
         setEventSource(events);
         return this;
     }
 
     public FluentBindingImpl<X, Y> twin(FluentBinding<Y, X> twin) {
 
         if (this.twin == null) {
             this.twin = twin;
             twin.twin(this);
         }
 
         return this;
     }
 
     /* (non-Javadoc)
      * @see little.nj.gui.binding.GenericBindingImpl#bind()
      */
     @Override
     public void bind() {
         disableTwin();
         super.bind();
         enableTwin();
     }
 
     private void setEventSource(BindingEventSource events) {
         if (this.events != null)
            events.ignore(listener);
 
         this.events = events;
 
         if (this.events != null)
            events.listen(listener);
     }
 
     private void disableTwin() {
         if (twin != null)
             twin.setEnabled(false);
     }
 
     private void enableTwin() {
         if (twin != null)
             twin.setEnabled(true);
     }
 
     private BindingListener listener = new BindingListener() {
 
         @Override
         public void handleBindingEvent(BindingEvent x) {
             bind();
         }
 
     };
 }
