 
 package org.reprap.geometry.polygons;
 
 import java.util.*;
 
 public class TestMain
 {
 	public static void rrpTest()
 	{
 		Rr2Point p = new Rr2Point(0.1, 0.15);
 		Rr2Point q = new Rr2Point(0.2, 0.85);
 		Rr2Point r = new Rr2Point(1, 0.89);
 		
 		Rr2Point s = new Rr2Point(0.95, 0.03);
 		Rr2Point pp = new Rr2Point(0.35, 0.62);
 		Rr2Point qq = new Rr2Point(0.55, 0.65);
 		Rr2Point rr = new Rr2Point(0.45, 0.5);
 		Rr2Point ss = new Rr2Point(0.4, 0.3);    
 		RrLine x = new RrLine(new Rr2Point(-1, -1), new Rr2Point(1, 1));
 		
 		RrPolygon a = new RrPolygon();
 		a.add(p, 1);
 		a.add(q, 1);
 		a.add(r, 1);
 		a.add(s, 1);
 		
 		RrPolygonList c = new RrPolygonList();
 		c.add(a);
 		
 		a = new RrPolygon();
 		a.add(rr, 2);
 		a.add(qq, 2);
 		a.add(pp, 2);
 		a.add(ss, 2);
 		c.add(a);
 		
 		//RrPolygonList d = c.offset(0.03);
 		
 		RrPolygon  e = c.hatch(x, 0.03, 3, 4);
 		//d = d.offset(0.003);
 		//e = e.join_up(d);
 		//c.add(d); 
 		c.add(e);
 		
 		new RrGraphics(c, false);
 	}
 	
 	public static RrCSGPolygon testPol()
 	{
 		Rr2Point p = new Rr2Point(0.1, 0.15);
 		Rr2Point q = new Rr2Point(0.2, 0.85);
 		Rr2Point r = new Rr2Point(0.97, 0.89);
 		Rr2Point s = new Rr2Point(0.95, 0.03);
 		
 		Rr2Point pp = new Rr2Point(0.35, 0.62);
 		Rr2Point qq = new Rr2Point(0.55, 0.95);
 		Rr2Point rr = new Rr2Point(0.45, 0.5);    
 		
 		RrHalfPlane ph = new RrHalfPlane(p, q);
 		RrHalfPlane qh = new RrHalfPlane(q, r);
 		RrHalfPlane rh = new RrHalfPlane(r, s);
 		RrHalfPlane sh = new RrHalfPlane(s, p);
 		
 		RrHalfPlane pph = new RrHalfPlane(pp, qq);
 		RrHalfPlane qqh = new RrHalfPlane(qq, rr);
 		RrHalfPlane rrh = new RrHalfPlane(rr, pp);
 		
 		RrCSG pc = new RrCSG(ph);
 		RrCSG qc = new RrCSG(qh);
 		RrCSG rc = new RrCSG(rh);
 		RrCSG sc = new RrCSG(sh);
 		
 		pc = RrCSG.intersection(pc, qc);
 		rc = RrCSG.intersection(sc, rc);		
 		pc = RrCSG.intersection(pc, rc);
 		
 		RrCSG ppc = new RrCSG(pph);
 		RrCSG qqc = new RrCSG(qqh);
 		RrCSG rrc = new RrCSG(rrh);
 		
 		ppc = RrCSG.intersection(ppc, qqc);
 		ppc = RrCSG.intersection(ppc, rrc);
 		ppc = RrCSG.difference(pc, ppc);
 		
		pc = ppc.offset(-0.09);
 		ppc = RrCSG.difference(ppc, pc);
 		
 		return new RrCSGPolygon(ppc, new 
 				RrBox(new Rr2Point(0,0), new Rr2Point(1.1,1.1)));
 	}
 	
 	public static void rrCSGTest()
 	{
 		RrCSGPolygon cp = testPol();
 		
 		cp.divide(1.0e-6, 1.0);
 		//System.out.println(cp.toString());
 		RrGraphics g = new RrGraphics(new 
 				RrBox(new Rr2Point(0,0), new Rr2Point(1,1)), true);
 		
 		//g.addCSG(cp);
 		
 		RrLine x = new RrLine(new Rr2Point(-1, -1), new Rr2Point(1, 1));
 		RrPolygon  h = cp.hatch_join(x, 0.005, 1, 3);
 		RrPolygonList hp;
 		hp = cp.megList(1, 0);
 		hp.add(h);
 		g.addPol(hp);  
 	}
 	
 	public static void rrCHTest()
 	{
 		RrCSGPolygon cp = testPol();
 		
		cp.divide(1.0e-6, 1.01);
 		RrPolygonList hp;
 		hp = cp.megList(4, 3);
		//System.out.println("polygons: " + hp.size());
 		RrPolygonList hpl0 = new RrPolygonList();
		//hpl0.add(hp.polygon(0));
 		hpl0.add(hp.polygon(1));
 		hpl0.add(hp.polygon(2));
 		RrCSGPolygon restored = hpl0.toCSG();
		restored.divide(1.0e-6, 1);
 		//System.out.println(restored.toString());
 		RrGraphics g = new RrGraphics(new 
 				RrBox(new Rr2Point(0,0), new Rr2Point(1,1)), true);
		//g.addPol(hp);
 		//g.addCSG(cp);
 		g.addCSG(restored);
 		
 //		List chl = hp.convexHull();
 //		RrPolygonList ch = new RrPolygonList();		
 //		ch.add(hp.toRrPolygonHull(chl, 1));
 //		g.addPol(ch);  
 	}
 	
 	public static void main(String args[])
 	{
 		//rrCSGTest();
 		rrCHTest();
 		//rrpTest();
 	}
 }
