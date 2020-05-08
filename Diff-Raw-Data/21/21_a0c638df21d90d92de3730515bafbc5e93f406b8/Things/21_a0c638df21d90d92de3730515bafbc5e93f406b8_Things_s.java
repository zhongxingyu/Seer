 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fibers;
 
 import java.security.interfaces.DSAKey;
 import java.util.ArrayList;
 import java.awt.*;
 import javax.sound.midi.*;// MidiDevice;
 
 /**
  *
  * @author human
  */
 public class Things {
 
   public static int ndims_init = 1;// 3 dimensions.  First dimension is a gimme, becuase it is the output/fire value.
   /*
    * ***************************************************************************************************
    */
 
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
     }/*
      * Magnitude_Squared
      */
 
 
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
           boolean nop = true;
         }
       }
     }
 
     public void CheckInf() {
       for (int cnt = 0; cnt < ndims; cnt++) {
         if (java.lang.Double.isInfinite(loc[cnt])) {
           boolean nop = true;
         }
       }
     }
     /*
      * ***************************************************************************************************
      */
 
     public boolean CheckVert() {
       boolean flat = true;
       for (int dcnt = 0; dcnt < ninputs; dcnt++) {
         flat &= (this.loc[dcnt] == 0.0);
       }
       //flat &= (pnt.loc[ninputs] != 0.0);
       if (flat) {
         boolean nop = true;
       }
       for (int cnt = 0; cnt < ndims; cnt++) {
         if (this.loc[cnt] == 1.0) {// orthogonality test
           boolean nop = true;
         }
       }
       return flat;
     }
     /*
      * ***************************************************************************************************
      */
 
     public void Get_Cross_Product(PointNd a, PointNd b) {
       this.Clear();
       this.loc[0] = (a.loc[1] * b.loc[2] - a.loc[2] * b.loc[1]);
       this.loc[1] = (a.loc[2] * b.loc[0] - a.loc[0] * b.loc[2]);
       this.loc[2] = (a.loc[0] * b.loc[1] - a.loc[1] * b.loc[0]);
     }
     /*
      * ***************************************************************************************************
      */
 
     public void Normal_To_Plane(PointNd plane) {// take the normal, and get the formula of the plane (x y z), with respect to z (or last dimension)
       double height = this.loc[ninputs];
       if (height == 0.0) {
         height = java.lang.Double.MIN_VALUE;
       }
       for (int dimcnt = 0; dimcnt < ninputs; dimcnt++) {
         plane.loc[dimcnt] = (-this.loc[dimcnt] / height);// multiply each axis length by the slope for that axis
       }
     }
     /*
      * ***************************************************************************************************
      */
 
     public void Get_Steepest(PointNd steep) {// get the steepest line on a plane with respect to Z by rotating its normal 90 degrees.
       steep.Clear();
       double vertical = this.loc[ninputs]; // last dimension is the output 'Z' dim.
       double floorpotenuse = 0.0;// first get the floorpotenuse, length of my shadow on floor
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
   /*
    * ***************************************************************************************************
    */
 
   public interface Drawable {
 
     public void Draw_Me(TransForm tr, Graphics2D gr);
   }
 
   public interface Causal {
 
     void Collect_And_Fire();
 
     void Pass_Back_Corrector();
   }
   /*
    * ***************************************************************************************************
    */
 
   public static class PlaneNd extends PointNd {
     /*
      * ***************************************************************************************************
      */
 
     public PlaneNd(int num_dims) {
       super(num_dims);
     }
     /*
      * ***************************************************************************************************
      */
 
     public double Get_Height(PointNd pnt) {
       // get the height of this plane, at this point's coordinates
       double plane_hgt = 0;
       try {
         plane_hgt = this.loc[ninputs];// last dimension holds height offset
       } catch (Exception e) {
         boolean nop = true;
       }
       double height = 0.0;
       for (int dim = 0; dim < ninputs; dim++) {
         height += (pnt.loc[dim] * this.loc[dim]);// multiply each axis length by the slope for that axis
       }
       height += plane_hgt;// add in the base offset
       return height;
     }
     /*
      * ***************************************************************************************************
      */
 
     public void Plane_Ramp_To_Normal(PointNd norm) {// take the plane, and get the formula of the normal (x y z), with respect to z (or last dimension)
       for (int dimcnt = 0; dimcnt < ninputs; dimcnt++) {
         norm.loc[dimcnt] = (-this.loc[dimcnt]);
       }
       norm.loc[ninputs] = 1.0;// should return a normal above the plane
       norm.Unitize();
     }
   }
 
   /*
    * ***************************************************************************************************
    */
   public static class CPoint extends PointNd implements Drawable, Causal {
     /*
      * Control Point
      */
     int Num_Upstreamers = 0;
     public CPoint[] US, DS;
     public double OutFire;
     public double Corrector;
     PointNd screenloc = new PointNd(2);// temporary but often-reused
     PointNd attractor;
     double radius, diameter;
     public NodeBox Parent;
     /*
      * ***************************************************************************************************
      */
 
     public CPoint(NodeBox NewParent, int num_dims) {
       super(num_dims);
       attractor = new PointNd(num_dims);
       this.Parent = NewParent;
       for (int cnt = 0; cnt < num_dims; cnt++) {
         this.loc[cnt] = 0.5;
       }
       Num_Upstreamers = 0;
       US = new CPoint[this.ninputs];
       DS = new CPoint[2];
       radius = 2.0;
       diameter = radius * 2.0;
      OutFire=0;
     }
     /*
      * ***************************************************************************************************
      */
 
     public double Get_Height() {
       return this.loc[this.ninputs];
     }
 
     public double Get_Outfire() {
       return this.loc[this.ninputs];
     }
 
     public void Draw_Me(TransForm tr, Graphics2D gr) {
       tr.To_Screen(this.loc[0], this.loc[1], screenloc);
 
       float pntband = (float) ((this.loc[ninputs] + 1.0) / 2.0);// map to range 0.0 - 1.0
       float red = pntband, blue = (float) 1.0 - pntband;
       blue = blue < 0 ? 0 : (blue > 1 ? 1 : blue);
       red = red < 0 ? 0 : (red > 1 ? 1 : red);
       Color pnt_color = new Color(red, 0.0f, blue);
 
       gr.setColor(pnt_color); // need to add code to xmap color here
       gr.fillRect((int) (screenloc.loc[0] - radius), (int) (screenloc.loc[1] - radius), (int) diameter, (int) diameter);
 
       gr.setColor(Color.black);
       gr.drawRect((int) (screenloc.loc[0] - radius), (int) (screenloc.loc[1] - radius), (int) diameter, (int) diameter);
 
       /*
        * {
        * if (true) { double factor = 100.0; gr.setColor(Color.magenta); * draw
        * the movement line * gr.drawLine( (int) (xorg + Bounds.Rad(0) *
        * pnt.loc[0]), (int) (yorg + Bounds.Rad(1) * pnt.loc[1]), (int) (xorg +
        * Bounds.Rad(0) * (pnt.loc[0] + pnt.delta[0] * factor)), (int) (yorg +
        * Bounds.Rad(1) * (pnt.loc[1] + pnt.delta[1] * factor))); } }
        */
     }
 
     public void Collect_And_Fire() {
       Corrector = 0.0;
       NodeBox.Roto_Plane plane = this.Parent.planeform;
       double SumFire = 0.0;
       //Num_Upstreamers = US.length;
       for (int pcnt = 0; pcnt < Num_Upstreamers; pcnt++) {
         CPoint cpnt = this.US[pcnt];
         double infire = cpnt.Get_Outfire();
         /*
          * for my attraction point, make a vector of all the upstreamers outfire
          * values.
          */
         attractor.loc[pcnt] = infire;
         /*
          * for sum outfire, mult each inlinks fire value by the tilt of our
          * plane in that dimension. then add them.
          *
          */
         // more to go here
       }
      OutFire=this.loc[ninputs];
      for (int cnt=0;cnt<ninputs;cnt++){
        this.loc[cnt]=attractor.loc[cnt];
       }
     }
 
     public void Pass_Back_Corrector() {
       NodeBox.Roto_Plane plane = this.Parent.planeform;
       //if (this.Parent.Num_Us > 0) {// hack
         /*
        * First generate the corrector
        */
       PointNd pdesire = new PointNd(this.ndims);
       plane.Attract_Point(this, pdesire);
       for (int pcnt = 0; pcnt < this.ninputs; pcnt++) {
         CPoint upstreamer = this.US[pcnt];
         try {
           upstreamer.Gather_Corrector(pdesire.loc[pcnt]);
         } catch (Exception e) {
           boolean nop = true;
         }
       }
       //}
     }
 
     public void Gather_Corrector(double goal) {
       Corrector += goal;
       if (Corrector != 0.0) {
         boolean nop = true;
       }
     }
 
     public void Apply_Corrector() {
       if (this.DS.length > 0) {// hacky hack
         Corrector *= NodeBox.Roto_Plane.sigmoid_deriv(this.loc[ninputs]);
       }
       // this is all wrong.  
       this.loc[ninputs] += Corrector;
       //compileboom();// fix all this junk.
       /*
        * things to change: only apply deriv when collecting correctors backward
        */
     }
   }
   /*
    * ***************************************************************************************************
    */
 
   public static class CPoint_List extends ArrayList<CPoint> {
 
     public NodeBox Parent;
 
     public CPoint_List(NodeBox NewParent, int newsize) {
       this.Parent = NewParent;
       PointNd avg = new PointNd(ndims_init);
       for (int cnt = 0; cnt < newsize; cnt++) {
         CPoint pnt = new CPoint(this.Parent, ndims_init);
         pnt.Randomize(-1.0, 1.0);
         this.add(pnt);
       }
       avg.Multiply(newsize);// experiment to move all the points to the centroid
     }
     /*
      * ***************************************************************************************************
      */
 
     public void Get_Average(PointNd ret_avg) {
       ret_avg.Clear();
       for (PointNd pnt : this) {
         ret_avg.Add(pnt);
       }
       ret_avg.Multiply(1.0 / (double) this.size());
     }
     /*
      * ***************************************************************************************************
      */
 
     public void CheckNAN() {
       for (PointNd pnt : this) {
         pnt.CheckNAN();
       }
     }
   }/*
    * CPoint_List
    */
   /*
    * ***************************************************************************************************
    */
 
 
   public static class NodeBox implements Drawable, Causal {
 
     public CPoint_List CPoints;
     public int Num_Us, Num_Ds;
     public Roto_Plane planeform;
     public double xorg, yorg, xscale, yscale;
 
     public NodeBox() {
       xscale = yscale = 20.0;
       Num_Us = Num_Ds = 0;
       CPoints = new CPoint_List(this, 0);
       planeform = new Roto_Plane(3);
     }
     /*
      * ***************************************************************************************************
      */
 
     public static class Roto_Plane extends PlaneNd implements Drawable {
       // the purpose of this class is to represent a sigmoid plane, to fit it to points, and to fit points to it.
 
       private PointNd pingvec;
       PointNd normal = null;
       PointNd desire = null;
       /*
        * These values below should come from NodeBox context!
        */
       int xorg, yorg;
       Bounder Bounds;
 
       /*
        * ***************************************************************************************************
        */
       public Roto_Plane(int num_dims) {
         super(num_dims);
         pingvec = new PointNd(ndims);
         normal = new PointNd(ndims);
         desire = new PointNd(ndims);
         //this.Randomize(-0.1, 0.1);
         this.loc[0] = 1.0;
       }
       /*
        * ***************************************************************************************************
        */
 
       public double ActFun(double xin) {
         double OutVal;
         if (false) {
           return xin;
         } else {
           OutVal = xin / Math.sqrt(1.0 + xin * xin);/*
            * symmetrical sigmoid function in range -1.0 to 1.0.
            */
           return OutVal;
         }
         /*
          * double power = 2.0; OutVal = xin / Math.pow(1 +
          * Math.abs(Math.pow(xin, power)), 1.0 / power);
          */
       }
       /*
        * ***************************************************************************************************
        */
 
       public double Reverse_ActFun(double xin) {
         double OutVal;// from http://www.quickmath.com/webMathematica3/quickmath/page.jsp?s1=equations&s2=solve&s3=basic
         // reverse, inverse of sigmoid is:
         OutVal = xin / (Math.sqrt(Math.abs(xin * xin - 1.0)));
         return OutVal;
       }
       /*
        * ***************************************************************************************************
        */
 
       public static double sigmoid_deriv(double Value) { /*
          * modified for default return -dbr
          */
         /*
          * Given the unit's activation value and sum of weighted inputs, compute
          * the derivative of the activation with respect to the sum. Defined
          * types are SIGMOID (-1 to +1) and ASYMSIGMOID (0 to +1).
          *
          */
         Value = (Value + 1.0) / 2;
         double SigmoidPrimeOffset = 0.1;
         /*
          * asymmetrical sigmoid function in range 0.0 to 1.0.
          */
         double returnval = (SigmoidPrimeOffset + (Value * (1.0 - Value)));
         returnval *= 2.857142857142857;// modify to fit 1:1 slope of sigmooid
         return returnval * 1.0;
       }
       /*
        * ***************************************************************************************************
        */
 
       public void Ping(PointNd pnt) {
         double phgt = pnt.loc[ninputs];
         // first get the height of the plane at this loc,
         double result;
         result = this.Get_Height(pnt);
         result = this.ActFun(result);
         // corrector is the distance from the sigmoid plane TOWARD this point's height
         double corr = phgt - result;
 
         double bell = Roto_Plane.sigmoid_deriv(phgt);// sigmoid derivative is a bell curve 
         corr *= bell;
 
         // now create the recognition vector, based on this pnt's position, and 1.0 * this plane's height offset dimension
         for (int cnt = 0; cnt < ninputs; cnt++) {
           pingvec.loc[cnt] = pnt.loc[cnt];
         }
         pingvec.loc[ninputs] = 1.0;
         Train_Inlinks(pingvec, ndims, 0.01, corr);
       }
       /*
        * ***************************************************************************************************
        */
 
       public double Get_Sigmoid_Height(PointNd pnt) {
         double hgt = this.Get_Height(pnt);
         hgt = this.ActFun(hgt);
         return hgt;
       }
       /*
        * ***************************************************************************************************
        */
 
       public void Attract_Point(PointNd pnt, PointNd pdesire) {
         double shadow_hgt = this.Get_Height(pnt);// height on raw plane at this point's position.
         double sigmoid_shadow_hgt = this.ActFun(shadow_hgt);// height on sigmoid plane at this point's position.
         //sigmoid_shadow_hgt = shadow_hgt;
         double delta_hgt = sigmoid_shadow_hgt - pnt.loc[ninputs];// distance from this point to sigmoid plane.
 
         PointNd nrm = new PointNd(ndims);
         this.Plane_Ramp_To_Normal(nrm);// get normal to raw plane.
         {
           pdesire.Clear();
           pdesire.loc[ninputs] = delta_hgt;
           double corrlen = pdesire.Dot_Product(nrm);// project delta onto normal, to get straight distance to plane.
           nrm.Multiply(corrlen);// multiply unit normal by corrector length to get correction vector.
           pdesire.Copy_From(nrm);
         }
         double vfactor = 0.0;// 0.09 * 0.3;// works better with 7 layers, and maybe 3
         pdesire.loc[ninputs] *= vfactor;
         // double jitamp = 0.0001; pdesire.Jitter(-jitamp, jitamp);
       }
       /*
        * ***************************************************************************************************
        */
 
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
       }/*
        * Train_Inlinks
        */
       /*
        * ***************************************************************************************************
        */
 
 
       public void Draw_Me(TransForm tr, Graphics2D gr) {
         TransForm mytrans = new TransForm();
         mytrans.Accumulate(tr, this.xorg, this.yorg, 1.0, 1.0);
         Plot_Gradient(tr, gr);
       }
 
       public void Plot_Gradient(TransForm tr, Graphics2D g2) {
         /*
          * all about the gradient for display
          */
         this.Plane_Ramp_To_Normal(normal);
         double hgt = this.loc[0];
         double grad_x0;
         double grad_y0;
         double grad_x1, grad_y1;
         PointNd steepest = new PointNd(ndims);
         normal.Get_Steepest(steepest);
         if (normal.loc.length > 1) {
           boolean nop = true;
         } else {
           boolean nop = true;
         }
         double[] ratios = new double[ninputs];// the ratios are NOT the inverse slopes.  they are from the steepest line.
         for (int cnt = 0; cnt < ninputs; cnt++) {
           ratios[cnt] = steepest.loc[cnt] / steepest.loc[ninputs];// inverse slope for each shadow of the steepest
         }
         if (true) {
           double offset = this.loc[ninputs];
           double height0 = -1.0 - offset;
           double height1 = 1.0 - offset;
           double brad = tr.xscale;// Bounds.Rad(ninputs);
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
 
         gradient = new GradientPaint((int) (tr.xoffs + grad_x0), (int) (tr.yoffs + grad_y0), startColor, (int) (tr.xoffs + grad_x1), (int) (tr.yoffs + grad_y1), endColor);// A non-cyclic gradient
         g2.setPaint(gradient);
 
         if (true) {
           int rxorg = (int) (tr.xoffs - tr.xscale);
           int ryorg = (int) (tr.yoffs - tr.yscale);
           int rwdt = (int) (2.0 * tr.xscale);
           int rhgt = (int) (2.0 * tr.yscale);
           g2.fillRect(rxorg, ryorg, rwdt, rhgt);
           g2.setColor(Color.white);
           g2.drawLine((int) (tr.xoffs + grad_x0), (int) (tr.yoffs + grad_y0), (int) (tr.xoffs + grad_x1), (int) (tr.yoffs + grad_y1));
         } else {
           g2.fillRect(xorg + (int) Bounds.minmax[0][0], yorg + (int) Bounds.minmax[0][1], (int) Bounds.Wdt(), (int) Bounds.Hgt());
           g2.setColor(Color.white);
           g2.drawLine(xorg + (int) grad_x0, yorg + (int) grad_y0, xorg + (int) grad_x1, yorg + (int) grad_y1);
         }
       }
     }
 
     public void Draw_Me(TransForm tr, Graphics2D gr) {
       TransForm mytrans = new TransForm();
       mytrans.Accumulate(tr, this.xorg, this.yorg, this.xscale, this.yscale);
 
       PointNd boxmin = new PointNd(2);
       PointNd boxmax = new PointNd(2);
       double xmin = -1.0, ymin = -1.0, xmax = 1.0, ymax = 1.0;
       mytrans.To_Screen(xmin, ymin, boxmin);
       mytrans.To_Screen(xmax, ymax, boxmax);
 
       gr.setColor(Color.green);
       gr.drawRect((int) (boxmin.loc[0]), (int) (boxmin.loc[1]), (int) (boxmax.loc[0] - boxmin.loc[0]), (int) (boxmax.loc[1] - boxmin.loc[1]));
 
       this.planeform.Draw_Me(mytrans, gr);
 
       for (CPoint cpnt : CPoints) {
         cpnt.Draw_Me(mytrans, gr);
       }
     }
 
     public void Init_States(int num_states) {
       CPoint cpnt;
       double amp = 1.0;
      double arbitrary_output=-0.5;
       for (int pcnt = 0; pcnt < num_states; pcnt++) {
         cpnt = new CPoint(this, 3);
         cpnt.loc[0] = ((pcnt & 1) - 0.5) * amp;
         cpnt.loc[1] = (((pcnt >> 1) & 1) - 0.5) * amp;
         cpnt.loc[2] = arbitrary_output;//cpnt.loc[0] + cpnt.loc[1];
         CPoints.add(cpnt);
        arbitrary_output+=0.333;
       }
     }
 
     public void Init_Xor() {
       double amp = 2.0;
       int num_pnts = this.CPoints.size();
       for (int pcnt = 0; pcnt < num_pnts; pcnt++) {
         CPoint cpnt = this.CPoints.get(pcnt);
         int xorval = (pcnt & 1) ^ ((pcnt >> 1) & 1);
         cpnt.loc[cpnt.ninputs] = (((double) xorval) - 0.5) * amp;
       }
     }
 
     public void ConnectIn(NodeBox upstreamer) {
       int Num_CPoints = upstreamer.CPoints.size();
       for (int pcnt = 0; pcnt < Num_CPoints; pcnt++) {
         CPoint us_cpnt = upstreamer.CPoints.get(pcnt);
         CPoint my_cpnt = this.CPoints.get(pcnt);
         us_cpnt.DS[upstreamer.Num_Ds] = my_cpnt;
         my_cpnt.US[this.Num_Us] = us_cpnt;
         my_cpnt.Num_Upstreamers++;
       }
       this.Num_Us++;
       upstreamer.Num_Ds++;
       //planeform = new Roto_Plane(this.Num_Us + 1);// kludge.  
     }
 
     public void Collect_And_Fire() {
       int Num_CPoints = this.CPoints.size();
       for (int pcnt = 0; pcnt < Num_CPoints; pcnt++) {
         CPoint cpnt = this.CPoints.get(pcnt);
         cpnt.Collect_And_Fire();
       }
     }
 
     public void Pass_Back_Corrector() {
       if (this.Num_Us > 0) {// hack
         int Num_CPoints = this.CPoints.size();
         for (int pcnt = 0; pcnt < Num_CPoints; pcnt++) {
           CPoint cpnt = this.CPoints.get(pcnt);
           cpnt.Pass_Back_Corrector();
         }
       }
     }
 
     public void Apply_Corrector() {
       int Num_CPoints = this.CPoints.size();
       for (int pcnt = 0; pcnt < Num_CPoints; pcnt++) {
         CPoint cpnt = this.CPoints.get(pcnt);
         cpnt.Apply_Corrector();
       }
     }
 
     public void Ping() {
       int Num_CPoints = this.CPoints.size();
       for (int pcnt = 0; pcnt < Num_CPoints; pcnt++) {
         CPoint cpnt = this.CPoints.get(pcnt);
         this.planeform.Ping(cpnt);
       }
     }
   }
   /*
    * ***************************************************************************************************
    */
 
   public static class Network implements Drawable, Causal {
 
     public ArrayList<NodeBox> Node_List;
     public double xorg, yorg;
 
     public Network() {
       Node_List = new ArrayList<NodeBox>();
     }
     /*
      * ***************************************************************************************************
      */
 
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
         nb.Init_States(4);// must rethink this
         nb.xorg = (ncnt + 1.0) * 70.0;
         Node_List.add(nb);
       }
     }
 
     public void Init_Xor() {
       int num_nodes = this.Node_List.size();
       for (int ncnt = 0; ncnt < num_nodes; ncnt++) {
         NodeBox nb = this.Node_List.get(ncnt);
         nb.Init_Xor();
       }
     }
 
     public void Connect_From_Other(Network other) {
       /*
        * Connect all-to-all between two meshes
        */
       int num_my_nodes = this.Node_List.size();
       int num_other_nodes = other.Node_List.size();
 
       for (int ncnt0 = 0; ncnt0 < num_other_nodes; ncnt0++) {
         NodeBox us = other.Node_List.get(ncnt0);
         for (int ncnt1 = 0; ncnt1 < num_my_nodes; ncnt1++) {
           NodeBox ds = this.Node_List.get(ncnt1);
           ds.ConnectIn(us);
         }
       }
       for (int ncnt1 = 0; ncnt1 < num_my_nodes; ncnt1++) {
         NodeBox ds = this.Node_List.get(ncnt1);
         //ds.Init_States(4);// must rethink this
         //ds.Init_States(1 << ds.Num_Us);// wrong wrong wrong
       }
     }
 
     public void Collect_And_Fire() {
       int num_my_nodes = this.Node_List.size();
       for (int ncnt1 = 0; ncnt1 < num_my_nodes; ncnt1++) {
         NodeBox nb = this.Node_List.get(ncnt1);
         nb.Collect_And_Fire();
       }
     }
 
     public void Pass_Back_Corrector() {
       int num_my_nodes = this.Node_List.size();
       for (int ncnt1 = 0; ncnt1 < num_my_nodes; ncnt1++) {
         NodeBox nb = this.Node_List.get(ncnt1);
         nb.Pass_Back_Corrector();
         nb.Ping();
       }
     }
 
     public void Apply_Corrector() {
       int num_my_nodes = this.Node_List.size();
       for (int ncnt1 = 0; ncnt1 < num_my_nodes; ncnt1++) {
         NodeBox nb = this.Node_List.get(ncnt1);
         nb.Apply_Corrector();
       }
     }
   }
   /*
    * ***************************************************************************************************
    */
 
   public static class Layers implements Drawable, Causal {
 
     public ArrayList<Network> Network_List;
     public double xorg, yorg;
 
     public Layers() {
       Network_List = new ArrayList<Network>();
       xorg = 100;
       yorg = 100;
     }
 
     public void Make_Layers(int num_layers) {
       for (int lcnt = 0; lcnt < num_layers; lcnt++) {
         Network net = new Network();
         net.Make_Layer(2);
         net.yorg = lcnt * 70.0;
         Network_List.add(net);
       }
 
       Network net_prev = this.Network_List.get(0);
       for (int lcnt = 1; lcnt < num_layers; lcnt++) {
         Network net = this.Network_List.get(lcnt);
         net.Connect_From_Other(net_prev);
         net_prev = net;
       }
       /*
        * In the last layer, we want to set the desired output heights
        */
       Network net_last = this.Network_List.get(this.Network_List.size() - 1);
       net_last.Init_Xor();
     }
     /*
      * ***************************************************************************************************
      */
 
     public void Draw_Me(TransForm tr, Graphics2D gr) {
 
       TransForm mytrans = new TransForm();
       mytrans.Accumulate(tr, this.xorg, this.yorg, 1.0, 1.0);
 
       int num_layers = Network_List.size();
       for (int lcnt = 0; lcnt < num_layers; lcnt++) {
         Network net = this.Network_List.get(lcnt);
         net.Draw_Me(mytrans, gr);
         /*
          */
       }
     }
 
     public void Collect_And_Fire() {
       int num_layers = Network_List.size();
       for (int lcnt = 0; lcnt < num_layers; lcnt++) {
         Network net = this.Network_List.get(lcnt);
         net.Collect_And_Fire();
       }
     }
 
     public void Pass_Back_Corrector() {
       int num_layers = Network_List.size();
 
       int last_layer = num_layers - 1;
       for (int lcnt = last_layer; lcnt >= 0; lcnt--) {
         Network net = this.Network_List.get(lcnt);
         net.Pass_Back_Corrector();
       }
       if (false) {
         for (int lcnt = 0; lcnt < num_layers; lcnt++) {
           Network net = this.Network_List.get(lcnt);
           net.Pass_Back_Corrector();
         }
       }
     }
 
     public void Apply_Corrector() {
       int num_layers = Network_List.size();
       int last_layer = num_layers - 1;
       for (int lcnt = last_layer; lcnt >= 0; lcnt--) {
         Network net = this.Network_List.get(lcnt);
         net.Apply_Corrector();
       }
     }
 
     public void RunCycle() {
       this.Collect_And_Fire();
       this.Pass_Back_Corrector();
       this.Apply_Corrector();
     }
   }
   /*
    * ***************************************************************************************************
    */
 
   public static class TransForm {
 
     public double xoffs = 0.0, yoffs = 0.0;
     public double xscale = 1.0, yscale = 1.0;
 
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
   /*
    * ***************************************************************************************************
    */
 
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
   /*
    * ***************************************************************************************************
    */
   /*
    * cycle: find cpoint vector of attraction to plane tell upstreamer cpoint
    * heights to go there, they adjust upstreamer cpoint tells me to go there
    * find vector of attraction to plane again
    *
    *
    * What next? cpoints ping plane to bend it. cpoints pass back their desires.
    * when cpoints receive adjustment now, they must apply it to their heights.
    * no fire forward yet. when cpoints fire forward, the downstreamer must move
    * its location to match heights of inputs.
    *
    * repaint automatically.
    *
    * need to make special case for input nodes.
    *
    */
 }
