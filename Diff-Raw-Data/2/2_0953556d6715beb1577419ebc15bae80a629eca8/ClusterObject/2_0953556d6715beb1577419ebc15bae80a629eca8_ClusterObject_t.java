 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is Colin J. Fuller's code.
  *
  * The Initial Developer of the Original Code is
  * Colin J. Fuller.
  * Portions created by the Initial Developer are Copyright (C) 2011
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s): Colin J. Fuller
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  *
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.clustering;
 
 import org.apache.commons.math.geometry.Vector3D;
 
 
 /**
  * Representation of an Object identified in an image with some defined spatial position that can be clustered.
  * 
  * @author Colin J. Fuller
  */
 public class ClusterObject implements Positioned{
 
     private Cluster currentCluster;
     private int mostProbableCluster;
     private org.apache.commons.math.geometry.Vector3D centroid;
     private int nPixels;
     private double prob;
 
     /**
      * Constructs a default ClusterObject.
      */
     public ClusterObject() {
         prob = 0;
         nPixels = 0;
         mostProbableCluster = 0;
         centroid =null;
         currentCluster = null;
     }
 
     /**
      * Gets a reference to the Cluster in which the ClusterObject is currently contained.
      * @return  The Cluster containing the ClusterObject.
      */
     public Cluster getCurrentCluster() {
         return currentCluster;
     }
 
     /**
      * Sets the Cluster to which the ClusterObject is currently assigned.
      *
      * This operation will remove any previous assignment.
      *
      * @param currentCluster    the Cluster to which to assign the ClusterObject.
      */
     public void setCurrentCluster(Cluster currentCluster) {
         this.currentCluster = currentCluster;
     }
 
     /**
      * Gets the centroid of the ClusterObject.
      *
      * @return  A Vector3D containing the components of the centroid of the ClusterObject.
      */
     public Vector3D getCentroid() {
         return centroid;
     }
 
     /**
      * Sets the centroid of the ClusterObject to the specified Vector3D.
      *
      * Any existing centroid is discarded.
      *
      * @param centroid  The Vector3D to which to set the centroid.
      */
     public void setCentroid(Vector3D centroid) {
         this.centroid = centroid;
     }
 
     /**
      * Sets the centroid of the ClusterObject by its individual components.
      *
      * Any existing centroid is discarded. 
      *
      * @param x     The x-component of the centroid.
      * @param y     The y-component of the centroid.
      * @param z     The z-component of the centroid.
      */
     public void setCentroidComponents(double x, double y, double z) {
         this.centroid = new Vector3D(x, y, z);
     }
 
     /**
      * Gets the number of pixels contained in the ClusterObject in the original image.
      * @return  The number of pixels in the ClusterObject.
      */
     public int getnPixels() {
         return nPixels;
     }
 
     /**
      * Sets the number of pixels contained in the ClusterObject in the oridinal image.
      * @param nPixels   The number of pixels in the ClusterObject.
      */
     public void setnPixels(int nPixels) {
         this.nPixels = nPixels;
     }
 
     /**
      * Increments the number of pixels contained in the ClusterObject by 1.
      */
     public void incrementnPixels() {
         this.nPixels++;
     }
 
     /**
      * Gets the probability (density) of the ClusterObject being found at its location given the Cluster to which it is assigned.
      * @return  The probability of the ClusterObject.
      */
     public double getProb() {
         return prob;
     }
 
     /**
      * Sets the probability (density) of the ClusterObject being found at its location given the Cluster to which it is assigned.
      * @param prob  The probability of the ClusterObject.
      */
     public void setProb(double prob) {
         this.prob = prob;
     }
 
     /**
      * Gets the ID of the cluster that is most likely to contain the ClusterObject.
     * @return  The ID, as an int.
      */
     public int getMostProbableCluster() {
         return mostProbableCluster;
     }
 
     /**
      * Sets the ID of the cluster that is most likely to contain the ClusterObject.
      * @param mostProbableCluster
      */
     public void setMostProbableCluster(int mostProbableCluster) {
         this.mostProbableCluster = mostProbableCluster;
     }
 
     //interface Positioned implementations
 
     public Vector3D getPosition() {return this.getCentroid();}
 
     public double distanceTo(Positioned other) {
        return other.getPosition().add(-1.0, this.getPosition()).getNorm();
     }
 
 }
