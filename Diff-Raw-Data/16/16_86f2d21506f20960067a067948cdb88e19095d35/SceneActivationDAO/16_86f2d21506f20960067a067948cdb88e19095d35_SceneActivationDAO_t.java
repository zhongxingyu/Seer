 /**
  *  C-Nery - A home automation web application for C-Bus.
  *  Copyright (C) 2008,2009  Dave Oxley <dave@daveoxley.co.uk>.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.daveoxley.cnery.dao;
 
 import com.daveoxley.cnery.entities.Scene;
 import com.daveoxley.cnery.entities.SceneActivation;
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityNotFoundException;
 import javax.persistence.NoResultException;
 import org.jboss.seam.annotations.AutoCreate;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 
 /**
  *
  * @author Dave Oxley <dave@daveoxley.co.uk>
  */
 @Name("sceneActivationDAO")
 @AutoCreate
 public class SceneActivationDAO implements Serializable {
 
     @In
     protected EntityManager entityManager;
 
     public SceneActivation findSceneActivation(String sceneActivationId) {
         try {
             StringBuilder query = new StringBuilder("select sa from SceneActivation sa where sa.id = :sceneActivationId");
             return (SceneActivation) entityManager
                     .createQuery(query.toString())
                     .setParameter("sceneActivationId", sceneActivationId)
                     .getSingleResult();
         } catch (EntityNotFoundException ex) {
         } catch (NoResultException ex) {
         }
         return null;
     }
 
     public List<SceneActivation> findSceneActivations() {
         try {
             return entityManager
                     .createQuery("select sa from SceneActivation sa")
                     .getResultList();
         } catch (EntityNotFoundException ex) {
         } catch (NoResultException ex) {
         }
         return null;
     }
 
     public List<SceneActivation> findSceneActivationsByGroupAddress(String address) {
         try {
             return entityManager
                    .createQuery("select sa from SceneActivation sa where sa.groupAddress=:address")
                     .setParameter("address", address)
                     .getResultList();
         } catch (EntityNotFoundException ex) {
         } catch (NoResultException ex) {
         }
         return null;
     }
 
     public List<SceneActivation> findSceneActivationsByScene(Scene scene) {
         try {
             return entityManager
                     .createQuery("select sa from SceneActivation sa where sa.scene=:scene")
                     .setParameter("scene", scene)
                     .getResultList();
         } catch (EntityNotFoundException ex) {
         } catch (NoResultException ex) {
         }
         return null;
     }
 }
