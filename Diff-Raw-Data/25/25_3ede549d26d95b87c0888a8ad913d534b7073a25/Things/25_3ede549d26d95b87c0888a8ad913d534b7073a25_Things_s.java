 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fibers;
 
 import java.security.interfaces.DSAKey;
 import java.util.ArrayList;
 import java.awt.*;
 
 /**
  *
  * @author human
  */
 public class Things {
 
   public static int ndims_init = 1;// 3 dimensions.  First dimension is a gimme, becuase it is the output/fire value.
   /* *************************************************************************************************** */
 
   public static class PointNd {
 
     public int ndims = 3;
     public int ninputs = ndims - 1;
     public double[] loc = null;
 
     public PointNd(int num_dims) {
       ndims = num_dims;
       ninputs = ndims - 1;
       loc = new double[ndims];
       Clear();
     }
 
     public double getloc(int dim) {
       return loc[dim];
     }
 
     public void setloc(int dim, double value) {
       loc[dim] = value;
     }
 
     public void Add(PointNd other) {
       for (int cnt = 0; cnt < ndims; cnt++) {
         loc[cnt] += other.loc[cnt];
       }
     }
 
     public void Subtract(PointNd other) {
       for (int cnt = 0; cnt < ndims; cnt++) {
         loc[cnt] -= other.loc[cnt];
       }
     }
 
     public void Copy_From(PointNd other) {
       for (int cnt = 0; cnt < ndims; cnt++) {
         loc[cnt] = other.loc[cnt];
       }
     }
 
     public void Multiply(double factor) {
       for (int cnt = 0; cnt < ndims; cnt++) {
         loc[cnt] *= factor;
       }
     }
 
     public double Magnitude(int dimensions) {
       double sumsq = 0.0;
       for (int cnt = 0; cnt < dimensions; cnt++) {
         sumsq += loc[cnt] * loc[cnt];
       }
       return Math.sqrt(sumsq);
     }
     PointNd another;
 
     public double Get_Distance(PointNd other, int dimensions) {
       another = other;
       double delta, dist = 0.0;// pythagorean distance
       for (int cnt = 0; cnt < dimensions; cnt++) {
         delta = other.loc[cnt] - this.loc[cnt];
         dist += delta * delta;// sum of the squares
       }
       dist = Math.sqrt(dist);
       return dist;
     }
     //--------------------------------------------------------------------
 
     public double Magnitude_Squared(int dimensions) {
       double sumsq = 0.0;
       for (int cnt = 0; cnt < dimensions; cnt++) {
         sumsq += loc[cnt] * loc[cnt];
       }
       return sumsq;
     }/* Magnitude_Squared */
 
 
     public void Get_Delta(PointNd other, int dimensions, PointNd pdelta) {
       pdelta.Clear();
       double delta;
       for (int cnt = 0; cnt < dimensions; cnt++) {
         delta = other.loc[cnt] - this.loc[cnt];
         pdelta.loc[cnt] = delta;
       }
     }
 
     public double Dot_Product(PointNd other) {
       double retval = 0.0;// assume other is a unit vector.
       for (int cnt = 0; cnt < ndims; cnt++) {
         if ((this.loc[cnt] != 0.0) && (other.loc[cnt] != 0.0)) {// Zero always wins, even against infinity in this usage.
           retval += this.loc[cnt] * other.loc[cnt];
         }
       }
       return retval;
     }
 
     public void Unitize() { // convert to unit vector
       double length = 0.0;
       for (int cnt = 0; cnt < ndims; cnt++) {
         double axis = this.loc[cnt];
         length += axis * axis;
       }
       length = Math.sqrt(length);//pythagoran length
       if (length == 0.0) {// fudge to avoid divide-by-zero
         length = java.lang.Double.MIN_VALUE;
       }
       for (int cnt = 0; cnt < ndims; cnt++) {
         this.loc[cnt] /= length;
       }
     }
 
     public void Randomize(double minval, double maxval) {
       double range = maxval - minval;
       for (int cnt = 0; cnt < ndims; cnt++) {
         loc[cnt] = minval + (Logic.wheel.nextDouble() * range);
       }
     }
 
     public void Jitter(double minval, double maxval) {
       double range = maxval - minval;
       for (int cnt = 0; cnt < ndims; cnt++) {
         loc[cnt] += minval + (Logic.wheel.nextDouble() * range);
       }
     }
 
     public void Clear() {
       for (int cnt = 0; cnt < ndims; cnt++) {
         loc[cnt] = 0.0;
       }
     }
 
     public void CheckNAN() {
       for (int cnt = 0; cnt < ndims; cnt++) {
         if (loc[cnt] != loc[cnt]) {
           boolean noop = true;
         }
       }
     }
 
     public void CheckInf() {
       for (int cnt = 0; cnt < ndims; cnt++) {
         if (java.lang.Double.isInfinite(loc[cnt])) {
           boolean noop = true;
         }
       }
     }
     /* *************************************************************************************************** */
 
     public boolean CheckVert() {
       boolean flat = true;
       for (int dcnt = 0; dcnt < ninputs; dcnt++) {
         flat &= (this.loc[dcnt] == 0.0);
       }
       //flat &= (pnt.loc[ninputs] != 0.0);
       if (flat) {
         boolean noop = true;
       }
       for (int cnt = 0; cnt < ndims; cnt++) {
         if (this.loc[cnt] == 1.0) {// orthogonality test
           boolean noop = true;
         }
       }
       return flat;
     }
     /* *************************************************************************************************** */
 
     public void Get_Cross_Product(PointNd a, PointNd b) {
       this.Clear();
       this.loc[0] = (a.loc[1] * b.loc[2] - a.loc[2] * b.loc[1]);
       this.loc[1] = (a.loc[2] * b.loc[0] - a.loc[0] * b.loc[2]);
       this.loc[2] = (a.loc[0] * b.loc[1] - a.loc[1] * b.loc[0]);
     }
     /* *************************************************************************************************** */
 
     public void Normal_To_Plane(PointNd plane) {// take the normal, and get the formula of the plane (x y z), with respect to z (or last dimension)
       double height = this.loc[ninputs];
       if (height == 0.0) {
         height = java.lang.Double.MIN_VALUE;
       }
       for (int dimcnt = 0; dimcnt < ninputs; dimcnt++) {
         plane.loc[dimcnt] = (-this.loc[dimcnt] / height);// multiply each axis length by the slope for that axis
       }
     }
     /* *************************************************************************************************** */
 
     public void Get_Steepest(PointNd steep) {// get the steepest line on a plane with respect to Z by rotating its normal 90 degrees.
       steep.Clear();
       double vertical = this.loc[ninputs]; // last dimension is the output 'Z' dim.
       double floorpotenuse = 0.0;// first get the floorpotenuse
       for (int cnt = 0; cnt < ninputs; cnt++) {
         double axis = this.loc[cnt];
         floorpotenuse += axis * axis;
       }
       floorpotenuse = Math.sqrt(floorpotenuse);
       steep.loc[ninputs] = floorpotenuse;// the result 'Z' value is the normal's floorpotenuse
       for (int cnt = 0; cnt < ninputs; cnt++) {
         double axis = this.loc[cnt];
         double ratio = (axis / floorpotenuse);
         steep.loc[cnt] = -vertical * ratio;// will always point up if normal points up.
       }
     }
   }
   /* *************************************************************************************************** */
 
   public interface Drawable {
 
     public void Draw_Me(TransForm tr, Graphics2D gr);
   }
   /* *************************************************************************************************** */
 
   public static class PlaneNd extends PointNd {
     /* *************************************************************************************************** */
 
     public PlaneNd(int num_dims) {
       super(num_dims);
     }
     /* *************************************************************************************************** */
 
     public double Get_Height(PointNd pnt) {
       // get the height of this plane, at this point's coordinates
       double plane_hgt = this.loc[ninputs];// last dimension holds height offset
       double height = 0.0;
       for (int dim = 0; dim < ninputs; dim++) {
         height += (pnt.loc[dim] * this.loc[dim]);// multiply each axis length by the slope for that axis
       }
       height += plane_hgt;// add in the base offset
       return height;
     }
     /* *************************************************************************************************** */
 
     public void Plane_Ramp_To_Normal(PointNd norm) {// take the normal, and get the formula of the plane (x y z), with respect to z (or last dimension)
       for (int dimcnt = 0; dimcnt < ninputs; dimcnt++) {
         norm.loc[dimcnt] = (-this.loc[dimcnt]);
       }
       norm.loc[ninputs] = 1.0;// should return a normal above the plane
       norm.Unitize();
     }
   }
 
   /* *************************************************************************************************** */
   public static class CPoint extends PointNd implements Drawable {/* Control Point */
     /* *************************************************************************************************** */
 
 
     public CPoint[] US, DS;
     PointNd screenloc = new PointNd(2);// temporary but often-reused
 
     public CPoint(int num_dims) {
       super(num_dims);
       for (int cnt = 0; cnt < num_dims; cnt++) {
         this.loc[cnt] = 0.5;
       }
       US = new CPoint[2];
       DS = new CPoint[2];
     }
     /* *************************************************************************************************** */
 
     public void Draw_Me(TransForm tr, Graphics2D gr) {
       tr.To_Screen(this.loc[0], this.loc[1], screenloc);
       gr.setColor(Color.red);
       gr.fillRect((int) (screenloc.loc[0]), (int) (screenloc.loc[1]), 3, 3);
     }
   }
   /* *************************************************************************************************** */
 
   public static class CPoint_List extends ArrayList<CPoint> {
     /* test comment */
 
     public CPoint_List(int newsize) {
       PointNd avg = new PointNd(ndims_init);
       for (int cnt = 0; cnt < newsize; cnt++) {
         CPoint pnt = new CPoint(ndims_init);
         pnt.Randomize(-1.0, 1.0);
         this.add(pnt);
       }
       avg.Multiply(newsize);// experiment to move all the points to the centroid
     }
     /* *************************************************************************************************** */
 
     public void Get_Average(PointNd ret_avg) {
       ret_avg.Clear();
       for (PointNd pnt : this) {
         ret_avg.Add(pnt);
       }
       ret_avg.Multiply(1.0 / (double) this.size());
     }
     /* *************************************************************************************************** */
 
     public void CheckNAN() {
       for (PointNd pnt : this) {
         pnt.CheckNAN();
       }
     }
   }/* CPoint_List */
   /* *************************************************************************************************** */
 
 
   public static class NodeBox implements Drawable {
 
     public CPoint_List CPoints;
     public int Num_Us, Num_Ds;
     public double xorg, yorg, xscale, yscale;
 
     public NodeBox() {
       xscale = yscale = 10.0;
       Num_Us = Num_Ds = 0;
       CPoints = new CPoint_List(0);
     }
     /* *************************************************************************************************** */
     public Roto_Plane planeform;
 
     public static class Roto_Plane extends PlaneNd {
       // the purpose of this class is to represent a sigmoid plane, to fit it to points, and to fit points to it.
 
       private PointNd pingvec;
       public double rangemin, rangectr, rangemax;
       PointNd running_avg = new PointNd(ndims);
       public double above, below;
       PointNd normal = new PointNd(ndims_init);
       PointNd desire = new PointNd(ndims_init);
       /* These values below should come from NodeBox context! */
       int xorg, yorg;
       Bounder Bounds;
 
 
       /* *************************************************************************************************** */
       public Roto_Plane(int num_dims) {
         super(num_dims);
         pingvec = new PointNd(ndims);
         rangemin = -1.0;
         rangectr = 0.0;
         rangemax = 1.0;
         above = 0.0;
         below = 0.0;
       }
       /* *************************************************************************************************** */
 
       public double ActFun(double xin) {
         double OutVal;
         if (false) {
           return xin;
         } else {
           OutVal = xin / Math.sqrt(1.0 + xin * xin);/* symmetrical sigmoid function in range -1.0 to 1.0. */
           return OutVal;
         }
         /*
          * double power = 2.0;
         OutVal = xin / Math.pow(1 + Math.abs(Math.pow(xin, power)), 1.0 / power);
          */
       }
       /* *************************************************************************************************** */
 
       public double Reverse_ActFun(double xin) {
         double OutVal;// from http://www.quickmath.com/webMathematica3/quickmath/page.jsp?s1=equations&s2=solve&s3=basic
         // reverse, inverse of sigmoid is:
         OutVal = xin / (Math.sqrt(Math.abs(xin * xin - 1.0)));
         return OutVal;
       }
       /* *************************************************************************************************** */
 
       public double sigmoid_deriv(double Value) { /* modified for default return  -dbr */
         /* Given the unit's activation value and sum of weighted inputs,
          *  compute the derivative of the activation with respect to the sum.
          *  Defined types are SIGMOID (-1 to +1) and ASYMSIGMOID (0 to +1).
          * */
         Value = (Value + 1.0) / 2;
         double SigmoidPrimeOffset = 0.1;
         /*  asymmetrical sigmoid function in range 0.0 to 1.0.  */
         double returnval = (SigmoidPrimeOffset + (Value * (1.0 - Value)));
         returnval *= 2.857142857142857;// modify to fit 1:1 slope of sigmooid
         return returnval * 1.0;
       }
       /* *************************************************************************************************** */
 
       public void Ping(PointNd pnt) {
         double phgt = pnt.loc[ninputs];
         // first get the height of the plane at this loc,
         double result;
         result = this.Get_Height(pnt);
         result = this.ActFun(result);
         // corrector is the distance from the sigmoid plane TOWARD this point's height
         double corr = phgt - result;
 
         double bell = this.sigmoid_deriv(phgt);
         corr *= bell;
 
         // now create the recognition vector, based on this pnt's position, and 1.0 * this plane's height offset dimension
         for (int cnt = 0; cnt < ninputs; cnt++) {
           pingvec.loc[cnt] = pnt.loc[cnt];
         }
         pingvec.loc[ninputs] = 1.0;
         Train_Inlinks(pingvec, ndims, 0.01, corr);
 
         double pdrop = (phgt * 0.1);
         this.rangectr = (this.rangectr * 0.9) + pdrop;
         if (true) {
           this.rangemax = ((this.rangemax - this.rangectr) * 0.9) + this.rangectr;
           this.rangemin = ((this.rangemin - this.rangectr) * 0.9) + this.rangectr;
           if (phgt < this.rangemin) {
             this.rangemin = phgt;
           }
           if (phgt > this.rangemax) {
             this.rangemax = phgt;
           }
         }
         /* keep an approximate centroid for the cloud of points that hit me */
         for (int cnt = 0; cnt < ninputs; cnt++) {
           running_avg.loc[cnt] *= 0.9;
           running_avg.loc[cnt] += 0.1 * pnt.loc[cnt];
         }
         above *= 0.9;
         below *= 0.9;
         if (pnt.loc[ninputs] > 0.0) {
           above += 0.1;
         } else if (pnt.loc[ninputs] < 0.0) {
           below += 0.1;
         }
         //this.Balance();
       }
       /* *************************************************************************************************** */
 
       public double Get_Sigmoid_Height(PointNd pnt) {
         double hgt = this.Get_Height(pnt);
         hgt = this.ActFun(hgt);
         return hgt;
       }
       /* *************************************************************************************************** */
 
       public void Balance() {
         double sub_ratio = above;// ratio of above-zero to total
         if (sub_ratio < 0.25 || 0.75 < sub_ratio) {
           double midval = this.Get_Sigmoid_Height(running_avg);
           double corr = 0.0 - midval;
           // whoops, need to make average be like pingvec, for the training
           running_avg.loc[ninputs] = 1.0;
           Train_Inlinks(running_avg, ndims, 0.1, corr);// was lrate of 0.01
         }
         if (false) {// another experiment, try to flatten the plane.
           for (int cnt = 0; cnt < ninputs; cnt++) {
             if (Math.abs(this.loc[cnt]) > 0.7) {
               this.loc[cnt] *= 0.999;
             }
           }
         }
       }
       /* *************************************************************************************************** */
 
       public void Attract_Point(PointNd pnt, PointNd pdesire) {
         double shadow_hgt = this.Get_Height(pnt);// height on raw plane at this point's position.
         double sigmoid_shadow_hgt = this.ActFun(shadow_hgt);// height on sigmoid plane at this point's position.
         //double sigmoid_shadow_hgt = this.Get_Sigmoid_Height(pnt);// height on sigmoid plane at this point's position.
 
         //sigmoid_shadow_hgt = shadow_hgt;
         double delta_hgt = sigmoid_shadow_hgt - pnt.loc[ninputs];// distance from this point to sigmoid plane.
 
         PointNd nrm = new PointNd(ndims);
         this.Plane_Ramp_To_Normal(nrm);// get normal to raw plane.
         nrm.Unitize();
         PointNd delta = new PointNd(ndims);// delta is the vector from the point to its place on the (sigmoid) plane
         delta.loc[ninputs] = delta_hgt;
         double corrlen = delta.Dot_Product(nrm);// project delta onto normal, to get straight distance to plane.
         delta.Copy_From(nrm);// multiply unit normal by corrector length to get correction vector.
         delta.Multiply(corrlen);
         pdesire.Copy_From(delta);
         if (false) {// flee the centroid
           delta.Copy_From(pnt);
           delta.Subtract(running_avg);
           if (delta.Magnitude(ninputs) <= 0.7) {
             delta.Multiply(0.01);
             delta.loc[ninputs] = 0.0;
             pdesire.Add(delta);
           }
         }
         if (false) {// flee the crack
           PointNd steep = new PointNd(ndims);
           normal.Get_Steepest(steep);
           steep.loc[ninputs] = 0;
           steep.Unitize();
           double sgn = Math.signum(pnt.loc[ninputs]);
           steep.Multiply(sgn);
           steep.Multiply(0.001);
           pdesire.Add(steep);
         }
         double range = Math.abs(this.rangemax - this.rangemin);
         double unitrange = (2.0 - range) / 2.0;// is the range as 0 to 1.0
         double finalrange = (0.9 * unitrange) + 0.1;//  this way the flattest ranges have the strongest self-inspired vertical attraction.
 
         double vfactor = finalrange;
         vfactor = 0.8;// works better with 2 layers
         vfactor = 0.09;// works better with 7 layers, and maybe 3
         //vfactor = 0.07;// test with 7
         vfactor *= 0.3;
         pdesire.loc[ninputs] *= vfactor;
 
         double jitamp = 0.0001;
         pdesire.Jitter(-jitamp, jitamp);
         //pdesire.loc[ninputs] = 0.0;
       }
       /* *************************************************************************************************** */
 
       public void Train_Inlinks(PointNd invec, int ninputs_local, double lrate, double corrector) {
         double invec_squared = invec.Magnitude_Squared(ninputs_local);
         if (invec_squared == 0.0) {
           invec_squared = Logic.fudge;
         }
         for (int cnt = 0; cnt < ninputs_local; cnt++) {
           double adj = (invec.loc[cnt] / invec_squared);// unitary adjustment tool
           adj = adj * corrector * lrate;
           this.loc[cnt] += adj;
         }
       }/* Train_Inlinks */
       /* *************************************************************************************************** */
 
 
       public void Plot_Gradient(Graphics2D g2) {
         /* all about the gradient for display */
         double hgt = this.loc[0];
         double grad_x0;
         double grad_y0;
         double grad_x1, grad_y1;
         PointNd steepest = new PointNd(ndims);
         normal.Get_Steepest(steepest);
         double[] ratios = new double[ninputs];// the ratios are NOT the inverse slopes.  they are from the steepest line.
         for (int cnt = 0; cnt < ninputs; cnt++) {
           ratios[cnt] = steepest.loc[cnt] / steepest.loc[ninputs];// inverse slope for each shadow of the steepest
         }
         if (true) {
           double offset = this.loc[ninputs];
           double height0 = -1.0 - offset;
           double height1 = 1.0 - offset;
           double brad = Bounds.Rad(ninputs);
           //gradx0 = (int) (brad * (height0 * ratios[0])); grad_y0 = (int) (brad * (height0 * ratios[1])); grad_x1 = (int) (brad * (height1 * ratios[0])); grad_y1 = (int) (brad * (height1 * ratios[1]));
           height0 *= brad;
           height1 *= brad;
           grad_x0 = (int) ((height0 * ratios[0]));
           grad_y0 = (int) ((height0 * ratios[1]));
           grad_x1 = (int) ((height1 * ratios[0]));
           grad_y1 = (int) ((height1 * ratios[1]));
         }
         Color startColor = new Color(0.0f, 0.0f, 1.0f);//Color startColor = Color.blue;
         Color endColor = new Color(1.0f, 0.0f, 0.0f);//Color endColor = Color.red;
 
         GradientPaint gradient;
         gradient = new GradientPaint(xorg + (int) grad_x0, yorg + (int) grad_y0, startColor, xorg + (int) grad_x1, yorg + (int) grad_y1, endColor);// A non-cyclic gradient
         g2.setPaint(gradient);
         g2.fillRect(xorg + (int) Bounds.minmax[0][0], yorg + (int) Bounds.minmax[0][1], (int) Bounds.Wdt(), (int) Bounds.Hgt());
         g2.setColor(Color.white);
         g2.drawLine(xorg + (int) grad_x0, yorg + (int) grad_y0, xorg + (int) grad_x1, yorg + (int) grad_y1);
       }
     }
 
     public void Draw_Me(TransForm tr, Graphics2D gr) {
       TransForm mytrans = new TransForm();
       mytrans.Accumulate(tr, this.xorg, this.yorg, this.xscale, this.yscale);
       for (CPoint cpnt : CPoints) {
         cpnt.Draw_Me(mytrans, gr);
       }
     }
 
     public void Init_States(int num_states) {
       CPoint cpnt;
       for (int pcnt = 0; pcnt < num_states; pcnt++) {
         cpnt = new CPoint(3);
         CPoints.add(cpnt);
       }
     }
 
     public void ConnectIn(NodeBox upstreamer) {
       int Num_CPoints = upstreamer.CPoints.size();
       for (int pcnt = 0; pcnt < Num_CPoints; pcnt++) {
         CPoint us_cpnt = upstreamer.CPoints.get(pcnt);
         CPoint my_cpnt = this.CPoints.get(pcnt);
         us_cpnt.DS[upstreamer.Num_Ds] = my_cpnt;
         my_cpnt.US[this.Num_Us] = us_cpnt;
       }
       this.Num_Us++;
       upstreamer.Num_Ds++;
     }
   }
   /* *************************************************************************************************** */
 
   public static class Network implements Drawable {
 
     public ArrayList<NodeBox> Node_List;
     public double xorg, yorg;
 
     public Network() {
       Node_List = new ArrayList<NodeBox>();
     }
     /* *************************************************************************************************** */
 
     public void Draw_Me(TransForm tr, Graphics2D gr) {
       TransForm mytrans = new TransForm();
       mytrans.Accumulate(tr, this.xorg, this.yorg, 1.0, 1.0);
       for (NodeBox node : Node_List) {
         node.Draw_Me(mytrans, gr);
       }
     }
 
     public void Make_Layer(int num_nodes) {
       for (int ncnt = 0; ncnt < num_nodes; ncnt++) {
         NodeBox nb = new NodeBox();
         nb.Init_States(4);
         Node_List.add(nb);
       }
     }
 
     public void Connect_From_Other(Network other) {
       /* Connect all-to-all between two meshes */
       int num_my_nodes = this.Node_List.size();
       int num_other_nodes = other.Node_List.size();
 
       for (int ncnt0 = 0; ncnt0 < num_other_nodes; ncnt0++) {
         NodeBox us = other.Node_List.get(ncnt0);
         for (int ncnt1 = 0; ncnt1 < num_my_nodes; ncnt1++) {
           NodeBox ds = this.Node_List.get(ncnt1);
           ds.ConnectIn(us);
         }
       }
     }
   }
   /* *************************************************************************************************** */
 
   public static class Layers implements Drawable {
 
     public ArrayList<Network> Network_List;
     public double xorg, yorg;
 
     public Layers() {
       Network_List = new ArrayList<Network>();
     }
 
     public void Make_Layers(int num_layers) {
       for (int lcnt = 0; lcnt < num_layers; lcnt++) {
         Network net = new Network();
         net.Make_Layer(2);
         Network_List.add(net);
       }
 
       Network net_prev = this.Network_List.get(0);
       for (int lcnt = 1; lcnt < num_layers; lcnt++) {
         Network net = this.Network_List.get(lcnt);
         net.Connect_From_Other(net_prev);
         net_prev = net;
       }
     }
     /* *************************************************************************************************** */
 
     public void Draw_Me(TransForm tr, Graphics2D gr) {
 
       TransForm mytrans = new TransForm();
       mytrans.Accumulate(tr, this.xorg, this.yorg, 1.0, 1.0);
 
       int num_layers = Network_List.size();
       for (int lcnt = 0; lcnt < num_layers; lcnt++) {
         Network net = this.Network_List.get(lcnt);
         net.Draw_Me(mytrans, gr);
         /*
          * so the question is how to put one drawing transform (at least offset) within another
          * pass coords live-time?
          * 
          * we want this to be simple.  so every layer has an org, every nodebox has a relaive org
          * inside the layer.  every cpoint has a relative org inside the nodebox.
          * 
          * maybe just pass the org offset as an object to the children in all draw_me calls?  yep.
          * 
          * everybody needs to think in their local coords.  
          * 
          * 
          */
       }
     }
   }
   /* *************************************************************************************************** */
 
   public static class TransForm {
 
    public static double xoffs, yoffs;
    public static double xscale = 1.0, yscale = 1.0;
 
     public void To_Screen(double xloc, double yloc, PointNd answer) {
       answer.loc[0] = xoffs + (xloc * xscale);
       answer.loc[1] = yoffs + (yloc * yscale);
     }
 
     public void Accumulate(TransForm parent, double xoffsp, double yoffsp, double xscalep, double yscalep) {
       // create my local transform by adding local context to parent context
       // wrong code, just a place holder
       PointNd org = new PointNd(2);
       parent.To_Screen(xoffsp, yoffsp, org);
       xoffs = org.loc[0];
       yoffs = org.loc[1];
       xscale = parent.xscale * xscalep;
       yscale = parent.yscale * yscalep;
     }
   }
   /* *************************************************************************************************** */
 
   public static class Bounder {
 
     double[][] minmax;// = new double[2][ndims];
 
     public Bounder(double rad, int ndims) {
       minmax = new double[2][ndims];
       for (int dcnt = 0; dcnt < ndims; dcnt++) {
         this.minmax[0][dcnt] = -rad; //dimension min
         this.minmax[1][dcnt] = rad; //dimension max        
       }
     }
 
     public double Wdt() {
       return minmax[1][0] - minmax[0][0];
     }
 
     public double Hgt() {
       return minmax[1][1] - minmax[0][1];
     }
 
     public double Dep() {
       return minmax[1][2] - minmax[0][2];
     }
 
     public double Sz(int dim) {
       return minmax[1][dim] - minmax[0][dim];
     }
 
     public double Rad(int dim) {
       return Sz(dim) / 2.0;
     }
 
     public double CtrX() {
       return (minmax[1][0] + minmax[0][0]) / 2.0;
     }
 
     public double CtrY() {
       return (minmax[1][1] + minmax[0][1]) / 2.0;
     }
 
     public double CtrZ() {
       return (minmax[1][2] + minmax[0][2]) / 2.0;
     }
 
     public double Ctr(int dim) {
       return (minmax[1][dim] + minmax[0][dim]) / 2.0;
     }
   }
   /* *************************************************************************************************** */
   /* 
   is there any harm in only applying correctors to *one* of the nodes in the output layer?
   we could also just make the one last node.  probably easiest.
    * 
    * every CPoint must have a fire value, its last dimension.
    * 
    * every nodebox must have a plane.
    * 
   we need graphic output early on.
    * 
    * 
    */
 }
