 /*
  * Copyright 2011, MyCellar
  *
  * This file is part of MyCellar.
  *
  * MyCellar is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * MyCellar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MyCellar. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.peralta.mycellar.interfaces.client.web.components.shared.set;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.apache.wicket.markup.repeater.RefreshingView;
 import org.apache.wicket.model.IModel;
 
 import fr.peralta.mycellar.domain.shared.IdentifiedEntity;
 
 /**
  * @author speralta
  */
 public abstract class SetView<E extends IdentifiedEntity> extends RefreshingView<E> {
 
     private static final long serialVersionUID = 201205301608L;
 
     /**
      * @param id
      * @param model
      */
     public SetView(String id, IModel<?> model) {
         super(id, model);
     }
 
     /**
      * @param id
      */
     public SetView(String id) {
         super(id);
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     @Override
     protected Iterator<IModel<E>> getItemModels() {
        return new SetModel<E>(((Collection<E>) getDefaultModelObject()).iterator());
     }
 
 }
