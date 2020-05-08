 
 import org.apache.commons.math.util.MathUtils;
 import java.util.List;
 import java.util.Iterator;
 import java.util.Vector;
 
 public class nrw_solver {
 
     private double c_r;
     private double c_w;
     private LatencyModel L_r;
     private LatencyModel L_w;
     private LatencyModel L_w_noack;
     private int w_min;
     private int n;
     private double t;
     private double p_s;
     private int k;
 
     private boolean solved = false;
 
     private List<nrw_solution> solutions;
 
     public nrw_solver(double p_s, double t, int k, double c_r, double c_w, LatencyModel L_r, LatencyModel L_w, LatencyModel L_w_noack, int w_min, int n)
     {
         this.c_r = c_r;
         this.c_w = c_w;
         this.L_r = L_r;
         this.L_w = L_w;
         this.L_w_noack = L_w;
         this.w_min = w_min;
         this.n  = n;
         this.p_s = p_s;
         this.t = t;
         this.k = k;
     }
 
     private double eval_func(int r, int w)
     {
         return c_r*r*calcReadLatency(r)+c_w*w*calcWriteLatency(w);
     }
 
     private static long fact(int n)
     {
         return MathUtils.factorial(n);
     }
 
     //this is p_w in the paper
     private double pdf_get_prob_w(int w, double t)
     {
          return ((double)fact(this.n))/(double)(fact(w-1)*fact(this.n-w))
                     * Math.pow(L_w_noack.getLatencyCDF(t), w-1)
                     * Math.pow(1-L_w_noack.getLatencyCDF(t), this.n-w)
                     * L_w.getLatencyPDF(t);
 
     }
 
     //This is P_w in the paper
     //calculate the probability of the (w-wmin)th slowest node of (n-wmin) nodes
     //having write by time t
     //from equation 6: http://mathworld.wolfram.com/OrderStatistic.html
     private double cdf_get_prob_w_after_write(int w, int wmin, double t)
     {
         return 1-Math.pow(1-Math.pow(L_w_noack.getLatencyCDF(t), w-wmin), MathUtils.binomialCoefficientDouble(n-wmin, w-wmin));
     }
 
     //this is p_r in the paper
     private double pdf_get_prob_r(int r, double t)
     {
         return ((double)fact(this.n))/(double)(fact(r-1)*fact(this.n-r))
                     * Math.pow(L_r.getLatencyCDF(t), r-1)
                     * Math.pow(1-L_r.getLatencyCDF(t), this.n-r)
                     * L_r.getLatencyPDF(t);
     }
 
     private double calcReadLatency(int r)
     {
         List<Double> domain = L_r.getRange();
 
         double accum = 0;
 
         for(Iterator<Double> it = domain.iterator(); it.hasNext();)
         {
             double t = it.next();
             accum += pdf_get_prob_r(r, t)*t;
         }
 
         return accum;
     }
 
     private double calcWriteLatency(int w)
     {
         List<Double> domain = L_w.getRange();
 
         double accum = 0;
 
         for(Iterator<Double> it = domain.iterator(); it.hasNext();)
         {
             double t = it.next();
             accum += pdf_get_prob_w(w, t)*t;
         }
 
         return accum;
     }
 
     private double calc_p_s_given_w(int rc, int wc)
     {
         //strong quorum --> fine
         if(rc+wc > n)
         {
             return 0.0;
         }
 
         return (MathUtils.binomialCoefficientDouble(n-wc, rc))
                         /MathUtils.binomialCoefficientDouble(n, rc);
     }
 
     private double calc_p_s(int rc, int wminc, double t)
     {
         double ret = (calc_p_s_given_w(rc, wminc));
 
         List<Double> times = L_w_noack.getRange();
 
         double last_prob = ret;
 
        for(int w = wminc; w<this.n; ++w)
         {
            ret += (calc_p_s_given_w(rc, w+1)-calc_p_s_given_w(rc, w))* cdf_get_prob_w_after_write(w, wminc, t);
         }
 
         if(ret > 1.0 || Math.round(ret*100000.0)/100000.0 < 0)
             throw new RuntimeException(String.format("p_s > 1; was %f\n", ret));
 
         return Math.pow(ret, k);
     }
 
     private void solve()
     {
         double cur_min = Double.POSITIVE_INFINITY;
 
         solutions = new Vector<nrw_solution>();
 
         for(int rc = 1; rc <= n; rc++)
         {
             for(int wc = w_min; wc <= n; wc++)
             {
                 double config_fitness = eval_func(rc, wc);
 
                 double this_ps = calc_p_s(rc, wc, t);
 
                 if(this_ps > p_s)
                 {
                     continue;
                 }
 
                 if(Math.round(config_fitness*1000) < Math.round(cur_min*1000))
                 {
                     solutions.clear();
                 }
                 if(Math.round(config_fitness*1000) <= Math.round(cur_min*1000))
                 {
                     solutions.add(new nrw_solution(n, rc, wc, calcReadLatency(rc), calcWriteLatency(wc), config_fitness, this_ps));
                     cur_min = config_fitness;
                 }
 
             }
         }
 
         solved = true;
     }
 
     public List<nrw_solution> get_solutions()
     {
         if(!solved)
             solve();
         return solutions;
     }
 }
