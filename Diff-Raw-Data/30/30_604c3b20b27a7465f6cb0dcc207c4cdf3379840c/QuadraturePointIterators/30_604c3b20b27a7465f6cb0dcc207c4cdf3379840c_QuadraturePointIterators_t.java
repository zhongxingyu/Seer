 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.epsilony.simpmeshfree.utils;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import net.epsilony.simpmeshfree.model.BoundaryCondition;
 import net.epsilony.simpmeshfree.model.LineBoundary;
 import net.epsilony.utils.geom.Coordinate;
 import net.epsilony.utils.geom.GeometryMath;
 import net.epsilony.utils.geom.Quadrangle;
 import net.epsilony.utils.geom.Triangle;
 import net.epsilony.utils.math.GaussLegendreQuadratureUtils;
 import net.epsilony.utils.math.QuadrangleMapper;
 import net.epsilony.utils.math.TriangleSymmetricQuadrature;
 
 /**
  *
  * @author epsilon
  */
 public class QuadraturePointIterators {
 
     public static class LineBoundaryIterator implements QuadraturePointIterator {
 
         Iterator<LineBoundary> lines;
         int quadNum;
         double[] weights;
         double[] coords;
         LineBoundary line;
         double lineLen;
         int cIdx;
         Coordinate linePar=new Coordinate();
 
         private void init(int power, Iterable<LineBoundary> lines) {
             this.lines = lines.iterator();
             int numOfPtsPerDim = (int) Math.ceil((power + 1) / 2.0);
             quadNum = numOfPtsPerDim;
             weights = GaussLegendreQuadratureUtils.getWeights(numOfPtsPerDim);
             coords = GaussLegendreQuadratureUtils.getPositions(numOfPtsPerDim);
             cIdx = quadNum;
         }
 
         public LineBoundaryIterator(int power, Iterable<LineBoundary> lines) {
             init(power, lines);
         }
 
         public LineBoundaryIterator(int power, LineBoundary[] lines) {
             init(power, Arrays.asList(lines));
         }
 
         protected void setLine(LineBoundary line) {
             this.line = line;
             lineLen=GeometryMath.distance(line.start, line.end);
         }
 
         @Override
         public boolean next(QuadraturePoint qp) {
             if (cIdx >= quadNum) {
                 if (!lines.hasNext()) {
                     return false;
                 }
                 setLine(lines.next());
                 cIdx = 0;
             }
             double u = coords[cIdx];
             double t = (u + 1) / 2.0;
             double weight = weights[cIdx] * lineLen / 2.0;
             linePar.x=t;
             Coordinate c = qp.coordinate;
             Coordinate start = line.start;
             Coordinate end = line.end;
             c.x = start.x * (1 - t) + end.x * t;
             c.y = start.y * (1 - t) + end.y * t;
             c.z = start.z * (1 - t) + end.z * t;
             qp.weight = weight;
             qp.boundary = line;
             cIdx++;
             return true;
         }
 
         protected Coordinate getBoundaryParameter() {
             return linePar;
         }
         
         
     }
 
     public static class LineBoundaryConditionIterator extends LineBoundaryIterator {
 
         BoundaryCondition bc;
         private boolean isCartesian;
 
         private void init(BoundaryCondition bc) {
             this.bc = bc;
         }
 
         public LineBoundaryConditionIterator(int power, Iterable<LineBoundary> lines, BoundaryCondition bc) {
             super(power, lines);
             init(bc);
         }
 
         public LineBoundaryConditionIterator(int power, LineBoundary[] lines,BoundaryCondition bc) {
             super(power, lines);
             init(bc);
         }
 
         @Override
         public boolean next(QuadraturePoint qp) {
             boolean res=super.next(qp);
             if(!res){
                 return false;
             }
             Coordinate input;
             if(isCartesian){
                 input=qp.coordinate;
             }else{
                 input=linePar;
             }
             bc.values(input, qp.values,qp.validities);
             return true;
         }
 
         @Override
         protected void setLine(LineBoundary line) {
             super.setLine(line);
             isCartesian=bc.setBoundary(line);
             
         }
         
         
     }
 
     public static class TriangleIterator implements QuadraturePointIterator {
 
         Iterator<Triangle> triangles;
         int power;
         private int numPtsPerTri;
         private Coordinate[] coords;
         private double[] weights;
         private int coordIdx;
         double triArea;
 
         private void init(int power,Iterable<Triangle> triangles) {
             this.triangles = triangles.iterator();
             numPtsPerTri = TriangleSymmetricQuadrature.getNumPoints(power);
             coords = new Coordinate[numPtsPerTri];
             for (int i = 0; i < coords.length; i++) {
                 coords[i] = new Coordinate();
             }
             weights = TriangleSymmetricQuadrature.getWeights(power);
             coordIdx = coords.length;
             this.power=power;
         }
 
         public TriangleIterator(int power,Triangle[] triangles) {
             init(power,Arrays.asList(triangles));
         }
 
         public TriangleIterator(int power, Iterable<Triangle> triangles) {
             init(power,triangles);
         }
 
         @Override
         public boolean next(QuadraturePoint qp) {
             if (coordIdx >= coords.length) {
                 if (!triangles.hasNext()) {
                     return false;
                 } else {
                     coordIdx = 0;
                     Triangle tri = triangles.next();
                     TriangleSymmetricQuadrature.getPositions(power, tri, coords);
                     triArea=GeometryMath.triangleArea2D(tri);
                 }
             }
             qp.coordinate.set(coords[coordIdx]);
             qp.weight = weights[coordIdx]*triArea;
             coordIdx++;
             return true;
         }
     }
 
     public static class QuadrangleIterator implements QuadraturePointIterator {
 
         Iterator<Quadrangle> quadrangles;
 
         private void init(int power, Iterable<Quadrangle> quadrangles) {
             this.quadrangles = quadrangles.iterator();
             int numOfPtsPerDim = (int) Math.ceil((power + 1) / 2.0);
             quadSize = numOfPtsPerDim;
             weights = GaussLegendreQuadratureUtils.getWeights(numOfPtsPerDim);
             uvs = GaussLegendreQuadratureUtils.getPositions(numOfPtsPerDim);
             uIdx = quadSize;
             vIdx = quadSize;
         }
 
         public QuadrangleIterator(int power, Iterable<Quadrangle> quadrangles) {
             init(power, quadrangles);
         }
 
         public QuadrangleIterator(int power, Quadrangle[] quadrangles) {
             init(power, Arrays.asList(quadrangles));
         }
         int uIdx;
         int vIdx;
         int quadIndex;
         int quadSize;
         double[] weights;
         double[] uvs;
         QuadrangleMapper quadMapper = new QuadrangleMapper();
         double[] xyJacb = new double[3];
        boolean isOff=false;
 
         @Override
         public boolean next(QuadraturePoint qp) {
            if(isOff){
                return false;
            }
             if (uIdx >= quadSize) {
                 uIdx = 0;
                 vIdx++;
                 if (vIdx >= quadSize) {
                     if (!quadrangles.hasNext()) {
                        isOff=true;
                         return false;
                     } else {
                         Quadrangle quad = quadrangles.next();
                         quadMapper.setVertices(quad.x1, quad.y1, quad.x2, quad.y2, quad.x3, quad.y3, quad.x4, quad.y4);
                         vIdx = 0;
                     }
                 }
             }
 
             double u = uvs[uIdx];
             double v = uvs[vIdx];
             quadMapper.getResults(u, v, xyJacb);
             qp.coordinate.x = xyJacb[0];
             qp.coordinate.y = xyJacb[1];
             qp.weight = xyJacb[2] * weights[uIdx] * weights[vIdx];
             uIdx++;
             return true;
         }
     }
     
     public static class CompoundIterator implements QuadraturePointIterator{
         Iterator<QuadraturePointIterator> iters;
         QuadraturePointIterator currentIter=null;
         
         
         @Override
         public boolean next(QuadraturePoint qp) {
            do{
                if(null==currentIter){
                    if(iters.hasNext()){
                        currentIter=iters.next();
                        continue;
                    }
                     return false;
                }else{
                    boolean res=currentIter.next(qp);
                    if(res){
                        return true;
                    }else{
                        currentIter=null;
                    }
                 }
            }while(true);
         }
         
         private void init(Iterable<QuadraturePointIterator> iters){
             this.iters=iters.iterator();
         }
         
         public CompoundIterator(Iterable<QuadraturePointIterator> iters){
             init(iters);
         }
     }
     
     public static QuadraturePointIterator compoundIterators(Iterable<QuadraturePointIterator> iters){
         return new CompoundIterator(iters);
     }
     
     public static QuadraturePointIterator fromLineBoundaries(int power,Iterable<LineBoundary> lines){
         return new LineBoundaryIterator(power, lines);
     }
     
     public static QuadraturePointIterator fromLineBoundariesAndBC(int power,Iterable<LineBoundary> lines,BoundaryCondition bc){
         return new LineBoundaryConditionIterator(power, lines, bc);
     }
     
     public static QuadraturePointIterator fromQuadrangles(int power,Iterable<Quadrangle> quads){
         return new QuadrangleIterator(power, quads);
     }
     
     public static QuadraturePointIterator fromTriangles(int power,Iterable<Triangle> tris){
         return new TriangleIterator(power, tris);
     }
 }
