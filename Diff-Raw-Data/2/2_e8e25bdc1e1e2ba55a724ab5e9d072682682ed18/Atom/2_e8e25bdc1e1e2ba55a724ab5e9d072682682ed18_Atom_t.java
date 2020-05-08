 import java.util.*;
 
 public class Atom {
    //change constructors
    
 	CartesianCoord coords;
     String atomType;
     int resNum;
     int bFactor;
     boolean isBackbone, nTerm, cTerm;
     
     public Atom(String atom, int resNum, boolean isBB, boolean nTerm, boolean cTerm, double tempFactor, CartesianCoord cc) {
 		atomType = atom;
 		coords = cc;
 		isBackbone = isBB;
     }
 
     public Atom(String atom, int resNum, boolean isBB) { //why are there two constructors with different parameters?
 		atomType = atom;
 		isBackbone = isBB;
     }
 
 	//constructor template
     public Atom() {
     }
 
     //acessor methods
     public String getAtomType() {
 		return atomType;
     }
 
     public CartesianCoord getCoords() {
 		return coords;
     }
     
     public boolean isBackboneAtom() {
     	return isBackbone;
     }
     
     public int getResNum() {
     	return resNum;
     }
 
     //mutator methods
     public void setAtomType(String AtomType) {
		atomType = AtomType;
     }
 
     public void setCoords(double x, double y, double z) {
 		coords = new CartesianCoord(x, y, z);
     }
 
     public void setCoords(CartesianCoord newCoord) {
 		coords = newCoord;
     }
     
     public void setBackbone(boolean isBB){
     	isBackbone = isBB;
     }
     
     public void setResNum(int newResNum){
     	resNum = newResNum;
     }
     
     public void setCTerm(boolean isCTerminus){
 	cTerm = isCTerminus;
     }
 }
