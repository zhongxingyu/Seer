 /*
  * Copyright (c) 2011-2012 Vikram Sundar.
  * All Rights Reserved.
  */
 package org.vikramdock;
 
 import java.io.*;
 import java.util.*;
 import java.util.zip.*;
 import java.lang.*;
 import java.lang.reflect.*;
 
 public class Atom {
 	double xcoord;
 	double ycoord;
 	double zcoord;
 	char element;
 	int resnum;
 	int atomnum;
 	int atomtype = -1;
 	char chainnum;
 	boolean spherical;
 	ArrayList<Bond> bonded = new ArrayList<Bond>();
 	String eType;
 	String AA;
 	public Atom(double xcoord, double ycoord, double zcoord, char element, int resnum, int atomnum, char chainnum, String eType, String AA) {
 		this.xcoord = xcoord;
 		this.ycoord = ycoord;
 		this.zcoord = zcoord;
 		this.element = element;
 		this.resnum = resnum;
 		this.atomnum = atomnum;
 		this.chainnum = chainnum;
 		this.spherical = false;
 		this.eType = eType;
 		this.AA = AA;
 	}
 	public Atom(double xcoord, double ycoord, double zcoord, char element, int resnum, int atomnum, char chainnum, String eType, ArrayList bonded, String AA) {
 		this.xcoord = xcoord;
 		this.ycoord = ycoord;
 		this.zcoord = zcoord;
 		this.element = element;
 		this.resnum = resnum;
 		this.atomnum = atomnum;
 		this.chainnum = chainnum;
 		this.spherical = false;
 		this.bonded = bonded;
 		this.eType = eType;
 		this.AA = AA;
 	}
 	public Atom(double xcoord, double ycoord, double zcoord, char element, int resnum, int atomnum, char chainnum, boolean spherical, String eType, String AA) {
 		this.xcoord = xcoord;
 		this.ycoord = ycoord;
 		this.zcoord = zcoord;
 		this.element = element;
 		this.resnum = resnum;
 		this.atomnum = atomnum;
 		this.chainnum = chainnum;
 		this.spherical = spherical;
 		this.eType = eType;
 		this.AA = AA;
 	}
 	public Atom(double xcoord, double ycoord, double zcoord, char element, int resnum, int atomnum, char chainnum, boolean spherical, String eType, ArrayList bonded, String AA) {
 		this.xcoord = xcoord;
 		this.ycoord = ycoord;
 		this.zcoord = zcoord;
 		this.element = element;
 		this.resnum = resnum;
 		this.atomnum = atomnum;
 		this.chainnum = chainnum;
 		this.spherical = spherical;
 		this.bonded = bonded;
 		this.eType = eType;
 		this.AA = AA;
 	}
 	public Atom(double xcoord, double ycoord, double zcoord, char element, int resnum, int atomnum, char chainnum, String eType, ArrayList bonded, String AA, int atomtype) {
 		this.xcoord = xcoord;
 		this.ycoord = ycoord;
 		this.zcoord = zcoord;
 		this.element = element;
 		this.resnum = resnum;
 		this.atomnum = atomnum;
 		this.chainnum = chainnum;
 		this.spherical = false;
 		this.bonded = bonded;
 		this.eType = eType;
 		this.AA = AA;
 		this.atomtype = atomtype;
 	}
 	public Atom(Atom clone) {
 		this.xcoord = clone.getXcoord();
 		this.ycoord = clone.getYcoord();
 		this.zcoord = clone.getZcoord();
 		this.element = clone.getElement();
 		this.resnum = clone.getResnum();
 		this.atomnum = clone.getAtomnum();
 		this.chainnum = clone.getChainnum();
 		this.spherical = clone.getSpherical();
 		this.bonded = clone.getBonded();
 		this.eType = clone.getEtype();
 		this.AA = clone.getAA();
 		this.atomtype = clone.getAtomType();
 	}
 	public void addBond(Bond toBeBonded) {
 		bonded.add(toBeBonded);
 	}
 	public void setAtomType(int atomtype) {
 		this.atomtype = atomtype;
 	}
 	public double getXcoord() {
 		return xcoord;
 	}
 	public double getYcoord() {
 		return ycoord;
 	}
 	public double getZcoord() {
 		return zcoord;
 	}
 	public double[] getCoords() {
 		double[] coords = new double[3];
 		coords[0] = xcoord;
 		coords[1] = ycoord;
 		coords[2] = zcoord;
 		return coords;
 	}
 	public char getElement() {
 		return element;
 	}
 	public int getResnum() {
 		return resnum;
 	}
 	public int getAtomnum() {
 		return atomnum;
 	}
 	public char getChainnum() {
 		return chainnum;
 	}
 	public boolean getSpherical() {
 		return spherical;
 	}
 	public ArrayList getBonded() {
 		return bonded;
 	}
 	public String getEtype() {
 		return eType;
 	}
 	public String getAA() {
 		return AA;
 	}
 	public int getAtomType() {
 		return atomtype;
 	}
 	public void printAtom() {
 		setCartesian();
 		System.out.println("ATOM NO " + atomnum + " FROM RES NO " + resnum + " FROM CHAIN NO " + chainnum + " ELEMENT " + element + " " + eType +  " COORDS " + xcoord + " " + ycoord + " " + zcoord);
 	}
 	public void printAtomErr() {
 		System.out.println("ATOM NO " + atomnum + " FROM RES NO " + resnum + " FROM CHAIN NO " + chainnum + " ELEMENT " + element + " " + eType + " COORDS " + xcoord + " " + ycoord + " " + zcoord + " SPHERICAL " + spherical);
 	}
 	public void printAtomPDB(PrintWriter out) {
 		setCartesian();
 		xcoord = round(xcoord, Math.pow(10,-3));
 		ycoord = round(ycoord, Math.pow(10,-3));
 		zcoord = round(zcoord, Math.pow(10,-3));
 		String toBePrinted = "ATOM  ";
 		int length = Integer.toString(atomnum).length();
 		if (length == 5) { 
 			toBePrinted = toBePrinted.concat(Integer.toString(atomnum));
 		} else if (length == 4) {
 			toBePrinted = toBePrinted.concat(" ").concat(Integer.toString(atomnum));
 		} else if (length == 3) {
 			toBePrinted = toBePrinted.concat("  ").concat(Integer.toString(atomnum));
 		} else if (length == 2) {
 			toBePrinted = toBePrinted.concat("   ").concat(Integer.toString(atomnum));
 		} else if (length == 1) {
 			toBePrinted = toBePrinted.concat("    ").concat(Integer.toString(atomnum));
 		}
 		toBePrinted = toBePrinted.concat("  ");
 		String atom = eType;
 		if (atom.length() ==  1) {
 			toBePrinted = toBePrinted.concat(atom).concat("   ");
 		} else if (atom.length() == 2) {
 			toBePrinted = toBePrinted.concat(atom).concat("  ");
 		} else if (atom.length() == 3) {
 			toBePrinted = toBePrinted.concat(atom).concat(" ");
 		}
 		toBePrinted = toBePrinted.concat(AA);
 		toBePrinted = toBePrinted.concat(" ").concat(Character.toString(chainnum));
 		String resnum = Integer.toString(this.resnum);
 		if (resnum.length() == 4) {
 			toBePrinted = toBePrinted.concat(resnum);
 		} else if (resnum.length() == 3) {
 			toBePrinted = toBePrinted.concat(" ").concat(resnum);
 		} else if (resnum.length() == 2) {
 			toBePrinted = toBePrinted.concat("  ").concat(resnum);
 		} else if (resnum.length() == 1) {
 			toBePrinted = toBePrinted.concat("   ").concat(resnum);
 		}
 		toBePrinted = toBePrinted.concat("    ");
 		String xcoordraw = Double.toString(xcoord);
 		String[] xcoords = xcoordraw.split("\\.");
 		String xcoordstr = null;
 		if (xcoords.length > 1) {
 			if (xcoords[1].length() > 3) {
 				xcoordstr = xcoords[0].concat(".").concat(xcoords[1].substring(0,3));
 			} else if (xcoords[1].length() == 3) {
 				xcoordstr = xcoordraw;
 			} else if (xcoords[1].length() == 2) {
 				xcoordstr = xcoordraw.concat("0");
 			} else {
 				xcoordstr = xcoordraw.concat("00");
 			}
 		} else {
 			xcoordstr = xcoordraw;
 		}
 		if (xcoordstr.length() == 8) {
 			toBePrinted = toBePrinted.concat(xcoordstr);
 		} else if (xcoordstr.length() == 7) {
 			toBePrinted = toBePrinted.concat(" ").concat(xcoordstr);
 		} else if (xcoordstr.length() == 6) {
 			toBePrinted = toBePrinted.concat("  ").concat(xcoordstr);
 		} else if (xcoordstr.length() == 5) {
 			toBePrinted = toBePrinted.concat("   ").concat(xcoordstr);
 		} else if (xcoordstr.length() == 4) {
 			toBePrinted = toBePrinted.concat("    ").concat(xcoordstr);
 		} else if (xcoordstr.length() == 3) {
 			toBePrinted = toBePrinted.concat("     ").concat(xcoordstr);
 		} else if (xcoordstr.length() == 2) {
 			toBePrinted = toBePrinted.concat("      ").concat(xcoordstr);
 		} else if (xcoordstr.length() == 1) {
 			toBePrinted = toBePrinted.concat("       ").concat(xcoordstr);
 		}
 		String ycoordraw = Double.toString(ycoord);
 		String[] ycoords = ycoordraw.split("\\.");
 		String ycoord = null;
 		if (ycoords.length > 1) {
 			if (ycoords[1].length() > 3) {
 				ycoord = ycoords[0].concat(".").concat(ycoords[1].substring(0,3));
 			} else if (ycoords[1].length() == 3) {
 				ycoord = ycoordraw;
 			} else if (ycoords[1].length() == 2) {
 				ycoord = ycoordraw.concat("0");
 			} else {
 				ycoord = ycoordraw.concat("00");
 			}
 		} else {
 			ycoord = ycoordraw;
 		}
 		if (ycoord.length() == 8) {
 			toBePrinted = toBePrinted.concat(ycoord);
 		} else if (ycoord.length() == 7) {
 			toBePrinted = toBePrinted.concat(" ").concat(ycoord);
 		} else if (ycoord.length() == 6) {
 			toBePrinted = toBePrinted.concat("  ").concat(ycoord);
 		} else if (ycoord.length() == 5) {
 			toBePrinted = toBePrinted.concat("   ").concat(ycoord);
 		} else if (ycoord.length() == 4) {
 			toBePrinted = toBePrinted.concat("    ").concat(ycoord);
 		} else if (ycoord.length() == 3) {
 			toBePrinted = toBePrinted.concat("     ").concat(ycoord);
 		} else if (ycoord.length() == 2) {
 			toBePrinted = toBePrinted.concat("      ").concat(ycoord);
 		} else if (ycoord.length() == 1) {
 			toBePrinted = toBePrinted.concat("       ").concat(ycoord);
 		} 
 		String zcoordraw = Double.toString(zcoord);
 		String[] zcoords = zcoordraw.split("\\.");
 		String zcoord = null;
 		if (zcoords.length > 1) {
 			if (zcoords[1].length() > 3) {
 				zcoord = zcoords[0].concat(".").concat(zcoords[1].substring(0,3));
 			} else if (zcoords[1].length() == 3) {
 				zcoord = zcoordraw;
 			} else if (zcoords[1].length() == 2) {
 				zcoord = zcoordraw.concat("0");
 			} else {
 				zcoord = zcoordraw.concat("00");
 			}
 		} else {
 			zcoord = zcoordraw;
 		}
 		if (zcoord.length() == 8) {
 			toBePrinted = toBePrinted.concat(zcoord);
 		} else if (zcoord.length() == 7) {
 			toBePrinted = toBePrinted.concat(" ").concat(zcoord);
 		} else if (zcoord.length() == 6) {
 			toBePrinted = toBePrinted.concat("  ").concat(zcoord);
 		} else if (zcoord.length() == 5) {
 			toBePrinted = toBePrinted.concat("   ").concat(zcoord);
 		} else if (zcoord.length() == 4) {
 			toBePrinted = toBePrinted.concat("    ").concat(zcoord);
 		} else if (zcoord.length() == 3) {
 			toBePrinted = toBePrinted.concat("     ").concat(zcoord);
 		} else if (zcoord.length() == 2) {
 			toBePrinted = toBePrinted.concat("      ").concat(zcoord);
 		} else if (zcoord.length() == 1) {
 			toBePrinted = toBePrinted.concat("       ").concat(zcoord);
 		}
 		toBePrinted = toBePrinted.concat("                       ");
 		toBePrinted = toBePrinted.concat(atom.substring(0,1));
 		out.println(toBePrinted);
 		out.flush();
 	}
 	public void setSpherical() {
 		if (spherical == false) {
 			spherical = true;
 			double xcoordold = xcoord;
 			double ycoordold = ycoord;
 			double zcoordold = zcoord;
 			xcoord = Math.sqrt((xcoordold * xcoordold) + (ycoordold * ycoordold) + (zcoordold * zcoordold));
 			ycoord = Math.atan2(ycoordold, xcoordold);
 			if (xcoord != 0.0) {
 				zcoord = Math.acos(zcoordold/xcoord);
 			} else {
 				zcoord = 0.0;
 			}    
 			System.out.flush();
 		}
 	}
 	public void setCartesian() {
 		if (spherical == true) {
 			spherical = false;
 			double xcoordold = xcoord;
 			double ycoordold = ycoord;
 			double zcoordold = zcoord;
 			xcoord = xcoordold * Math.cos(ycoordold) * Math.sin(zcoordold);
 			ycoord = xcoordold * Math.sin(ycoordold) * Math.sin(zcoordold);
 			zcoord = xcoordold * Math.cos(zcoordold);
 			System.out.flush();
 		}
 	}
 	public Atom transAtom(double xmov, double ymov, double zmov) {
 		setCartesian();
 		double xcoordnew = xmov + xcoord;
 		double ycoordnew = ymov + ycoord;
 		double zcoordnew = zmov + zcoord;
 		Atom answer = new Atom(xcoordnew, ycoordnew, zcoordnew, element, resnum, atomnum, chainnum, eType, bonded, AA, atomtype);
 		return answer;
 	}
 	public Atom rotateAtomNew(double xcent, double ycent, double zcent, double a, double b, double c) {
 		setCartesian();
 		double xcoorda = xcoord - xcent;
 		double ycoorda = ycoord - ycent;
 		double zcoorda = zcoord - zcent;
 		double xcoordb = xcoorda * Math.cos(b) * Math.cos(c) - zcoorda * Math.sin(b) + ycoorda * Math.cos(b) * Math.sin(c);
 		double ycoordb = ycoorda * Math.cos(a) * Math.cos(c) + zcoorda * Math.cos(b) * Math.sin(a) + xcoorda * Math.cos(c) * Math.sin(a) * Math.sin(b) - xcoorda * Math.cos(a) * Math.sin(c) + ycoorda * Math.sin(a) * Math.sin(b) * Math.sin(c); 
 		double zcoordb = zcoorda * Math.cos(a) * Math.cos(b) - ycoorda * Math.cos(c) * Math.sin(a) + xcoorda * Math.cos(a) * Math.cos(c) * Math.sin(b) + xcoorda * Math.sin(a) * Math.sin(c) + ycoorda * Math.cos(a) * Math.sin(b) * Math.sin(c);
 		xcoordb = round(xcoordb, Constants.FPPRECISION);
 		ycoordb = round(ycoordb, Constants.FPPRECISION);
 		zcoordb = round(zcoordb, Constants.FPPRECISION);
 		double xcoordc = xcoordb + xcent;
 		double ycoordc = ycoordb + ycent;
 		double zcoordc = zcoordb + zcent;
 		Atom answer = new Atom(xcoordc, ycoordc, zcoordc, element, resnum, atomnum, chainnum, eType, bonded, AA, atomtype);
		long end = System.currentTimeMillis();
 		return answer;
 	}
 	public double distance(Atom other) {
 		boolean changethis = false;
 		if (spherical) {
 			setCartesian();
 			changethis = true;
 		}
 		boolean changeother = false;
 		if (other.getSpherical()) {
 			other.setCartesian();
 			changeother = true;
 		}
 		double distance = Math.sqrt(Math.pow(xcoord - other.getXcoord(), 2) + Math.pow(ycoord - other.getYcoord(), 2) + Math.pow(zcoord - other.getZcoord(), 2));
 		if (changethis) {
 			setSpherical();
 		}
 		if (changeother) {
 			other.setSpherical();
 		}
 		return distance;
 	}
 	public double angle(Atom second, Atom third) {
 		double a = distance(second);
 		double b = distance(third);
 		double c = second.distance(third);
 		return Math.acos((Math.pow(a,2) + Math.pow(b,2) - Math.pow(c,2))/(2*a*b));
 	}
 	public double round(double number, double roundTo) {
 		return (double)(Math.round(number/roundTo)*roundTo);
 	}
 }
