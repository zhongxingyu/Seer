 /**
  * *******************************************************************************
  * Copyright (c) 2011, Monnet Project All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: *
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. * Redistributions in binary
  * form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. * Neither the name of the Monnet Project nor the names
  * of its contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *******************************************************************************
  */
 package eu.monnetproject.translation.phrasal.jmert;
 
 import java.util.Arrays;
 import org.apache.commons.math.linear.Array2DRowRealMatrix;
 import org.apache.commons.math.linear.LUDecompositionImpl;
 import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;
 
 /**
  * Derived from Olivier Chappelle's Matlab implementation. Based on "Efficient
  * algorithms for Ranking with SVMs", Chappelle & Keerthi (2009).
  *
  * @author John McCrae
  */
 public class SVMRank {
     // Configuration variables
     public int maxNewtonIterations = 10;
     public double prec = 1e-4;
     public double minresPrecision = 1e-3;
     public int minResIterations = 20;
     // Problem space variables
     double[] w;  // d
     double C;  // p
     double[] out; // p
     double[] step; // d
     double[][] A; // pxn
     int[][] spA; // px2
     double[][] X; // nxd
     int d;
     int p;
     int n;
     int[] sv; // p
     int svN; // < p
     double[] grad; // d
     double obj;
 
     /**
      * Create a Ranking SVM problem
      * @param X The vectors
      * @param A The matrix where {@code A[i][j] = 1} iff {@code rank(i) > rank(j)} and 
      * {@code A[i][j] = -1} iff {@code rank(i) < rank(j)} 
      * @param C The loss parameter
      */
     public SVMRank(double[][] X, double[][] A, double C) {
         this.X = X;
         this.A = A;
         this.C = C;
     }
 
     /**
      * Create a Ranking SVM problem
      * @param X The vectors
      * @param A A set of vectors where {@code spA[_] = { i, j } } iff {@code rank(i) > rank(j) }
      * @param C The loss parameter
      */
     public SVMRank(double[][] X, int[][] spA, double C) {
         this.X = X;
         this.spA = spA;
         this.C = C;
     }
 
     public double[] solve() {
         if (X.length == 0) {
             return new double[0];
         }
         d = X[0].length;
         n = X.length;
         final int k = Math.min(d, minResIterations);
         // Min Residual method has complexity O(k^2nd + pd)
         // LU method has complexity O(d^3 + pd)
         return solve(k * k * n < d * d);
     }
     
     public double[] solve(boolean minResidualMethod) {
         d = X[0].length;
         n = X.length;
         if (spA != null) {
             p = spA.length;
         } else {
             p = A.length;
         }
         if (p == 0) {
             return new double[d];
         }
         w = new double[d];
         Arrays.fill(w, 1.0);
         sv = new int[p];
         svN = 0;
         grad = new double[d];
         x0 = new double[d];
         step = new double[d];
 
         int iter = 0;
         out = new double[p];
 
         // out = 1 - A*(X*w)
         for (int i = 0; i < p; i++) {
             out[i] = 1;
             for (int j = 0; j < d; j++) {
                 if (spA != null) {
                     out[i] -= (X[spA[i][0]][j] - X[spA[i][1]][j]) * w[j];
                 } else {
                     for (int k = 0; k < n; k++) {
                         out[i] -= A[i][k] * X[k][j] * w[j];
                     }
                 }
             }
         }
 
         //while 1   
         while (true) {
             // iter = iter + 1;
             iter++;
             if (iter > maxNewtonIterations) {
                 System.err.println("Maximum number of Newton steps reached.Try larger lambda");
                 break;
             }
 
             //[obj, grad, sv] = obj_fun_linear(w,C,out);
             objFunLinear();
 
             if (minResidualMethod) {
                 //       [step, foo, relres] = minres(@hess_vect_mult, -grad,...
                 //                   opt.cg_prec,opt.cg_it,[],[],[],sv,C);
                 // minres(A,b,tolerance,itMax,...)
                 gmresHessMult();
             } else {
                 luHessSolve();
             }
 
            if(allZero(step)) {
                break;
            }
            
             // [t,out] = line_search_linear(w,step,out,C);
             double t = lineSearchLinear();
 
             // w = w + t*step;
             for (int i = 0; i < d; i++) {
                 w[i] = w[i] + t * step[i];
             }
 
             // if -step'*grad < opt.prec * obj  
             //  % Stop when the Newton decrement is small enough
             //  break;
             if (-1.0 * innerProduct(step, grad) < prec * obj) {
                 break;
             }
         }
         return w;
     }
 
     private void luHessSolve() {
         // Xsv = A(sv,:)*X;
         double[][] Xsv = new double[svN][d];
         for (int i = 0; i < svN; i++) {
             Xsv[i] = new double[d];
             if (spA != null) {
                 for (int k = 0; k < d; k++) {
                     Xsv[i][k] += X[spA[sv[i]][0]][k] - X[spA[sv[i]][1]][k];
                 }
             } else {
                 for (int j = 0; j < n; j++) {
                     for (int k = 0; k < d; k++) {
                         Xsv[i][k] += A[sv[i]][j] * X[j][k];
                     }
                 }
             }
         }
         double[][] hess = new double[d][d];
         //hess = eye(d) + Xsv'*(Xsv.*repmat(C(sv),1,d)); % Hessian
         for (int i = 0; i < d; i++) {
             hess[i][i] = 1;
             for (int j = 0; j < svN; j++) {
                 for (int k = 0; k < d; k++) {
                     hess[i][k] += Xsv[j][i] * Xsv[j][k] * C;
                 }
             }
         }
         //step  = - hess \ grad;   % Newton direction NB -1 * inv(A) * grad
         Arrays.fill(step, 0);
         final double[][] invHess = new LUDecompositionImpl(new Array2DRowRealMatrix(hess)).getSolver().getInverse().getData();
         for (int i = 0; i < d; i++) {
             for (int j = 0; j < d; j++) {
                 step[i] += -1.0 * invHess[i][j] * grad[j];
             }
         }
     }
 
     private void objFunLinear() {
         //out = max(0,out);
         for (int i = 0; i < out.length; i++) {
             out[i] = Math.max(0, out[i]);
         }
         //obj = sum(C.*out.^2)/2 + w'*w/2; % L2 penalization of the errors
         obj = 0.0;
         for (int i = 0; i < p; i++) {
             obj += (C * out[i] * out[i]) / 2.0;
         }
         for (int i = 0; i < d; i++) {
             obj += w[i] * w[i] / 2.0;
         }
         //grad = w - (((C.*out)'*A)*X)'; % Gradient
         for (int i = 0; i < d; i++) {
             grad[i] = w[i];
             for (int j = 0; j < p; j++) {
                 if (spA != null) {
                     grad[i] -= C * out[j] * (X[spA[j][0]][i] - X[spA[j][1]][i]);
                 } else {
                     for (int k = 0; k < n; k++) {
                         grad[i] -= C * out[j] * A[j][k] * X[k][i];
                     }
                 }
             }
         }
         //sv = out>0;  
         svN = 0;
         for (int i = 0; i < p; i++) {
             if (out[i] > 0) {
                 sv[svN++] = i;
             }
         }
     }
 
     private double lineSearchLinear() {
         int[] lsv = new int[p];
         int lsvN;
         // t = 0;
         double t = 0;
 
         // Precompute some dots products
         // Xd = A*(X*step);
         double[] Xd = new double[p];
         for (int i = 0; i < p; i++) {
             for (int k = 0; k < d; k++) {
                 if (spA != null) {
                     Xd[i] += (X[spA[i][0]][k] - X[spA[i][1]][k]) * step[k];
                 } else {
                     for (int j = 0; j < n; j++) {
                         Xd[i] += A[i][j] * X[j][k] * step[k];
                     }
                 }
             }
         }
         //  wd = w'*step;
         double wd = 0.0;
         for (int i = 0; i < d; i++) {
             wd += w[i] * step[i];
         }
         //  dd = step'*step;
         double dd = 0.0;
         for (int i = 0; i < d; i++) {
             dd += step[i] * step[i];
         }
         //  while 1
         while (true) {
             //    out2 = out - t*Xd; % The new outputs after a step of length t
             double[] out2 = new double[p];
             System.arraycopy(out, 0, out2, 0, p);
             for (int i = 0; i < p; i++) {
                 out2[i] = out[i] - t * Xd[i];
             }
 //    sv = find(out2>0);
             lsvN = 0;
             for (int i = 0; i < p; i++) {
                 if (out2[i] > 0) {
                     lsv[lsvN++] = i;
                 }
             }
 //    g = wd + t*dd - (C(sv).*out2(sv))'*Xd(sv); % The gradient (along the line)
             double g = wd + t * dd;
             for (int i = 0; i < lsvN; i++) {
                 g -= C * out2[lsv[i]] * Xd[lsv[i]];
             }
 //    h = dd + Xd(sv)'*(Xd(sv).*C(sv)); % The second derivative (along the line)
             double h = dd;
             for (int i = 0; i < lsvN; i++) {
                 h += Xd[lsv[i]] * Xd[lsv[i]] * C;
             }
 //    t = t - g/h; % Take the 1D Newton step. Note that if d was an exact Newton
             t = t - g / h;
 //                 % direction, t is 1 after the first iteration.
 //    if g^2/h < 1e-10, break; end;
             if (g * g / h < 1e-10) {
 //  out = out2;
                 out = out2;
                 return t;
             }
             //  end;
         }
     }
 
     public static double[] hessVectMult(double[][] X, int[][] spA, double[] w, int[] sv, int svN, double C) {
         int d = w.length;
         //int n = X.length;
         // y = w
         double[] y = new double[d];
         System.arraycopy(w, 0, y, 0, d);
 
         // z = (C.*sv).*(A*(X*w));  % Computing X(sv,:)*x takes more time in Matlab :-(
         double[] z = new double[svN];
         for (int i2 = 0; i2 < svN; i2++) {
             int i = sv[i2];
             for (int j = 0; j < d; j++) {
                 //if (spA != null) {
                 z[i2] += (X[spA[i][0]][j] - X[spA[i][1]][j]) * w[j];
                 //} else {
                 //    for (int k = 0; k < n; k++) {
                 //        z[i2] += A[i][k] * X[k][j] * w[j];
                 //    }
                 //}
             }
             z[i2] *= C;
         }
         // y = y + ((z'*A)*X)';
         for (int i = 0; i < d; i++) {
             for (int j2 = 0; j2 < svN; j2++) {
                 int j = sv[j2];
                 //if(spA != null) {
                 y[i] += 2 * (X[spA[j][0]][i] - X[spA[j][1]][i]) * z[j2];
                 //} else {
                 //   for (int k = 0; k < n; k++) {
                 //      y[i] += A[j][k] * X[k][i] * z[j2];
                 //  }
                 //}
             }
         }
 
         return y;
     }
 
     public static double[] hessVectMult(double[][] X, double[][] A, double[] w, int[] sv, int svN, double C) {
         int d = w.length;
         int n = X.length;
         // y = w
         double[] y = new double[d];
         System.arraycopy(w, 0, y, 0, d);
 
         // z = (C.*sv).*(A*(X*w));  % Computing X(sv,:)*x takes more time in Matlab :-(
         double[] z = new double[svN];
         for (int i2 = 0; i2 < svN; i2++) {
             int i = sv[i2];
             for (int j = 0; j < d; j++) {
                 for (int k = 0; k < n; k++) {
                     z[i2] += A[i][k] * X[k][j] * w[j];
                 }
             }
             z[i2] *= C;
         }
         // y = y + ((z'*A)*X)';
         for (int i = 0; i < d; i++) {
             for (int j2 = 0; j2 < svN; j2++) {
                 int j = sv[j2];
 
                 for (int k = 0; k < n; k++) {
                     y[i] += 2 * A[j][k] * X[k][i] * z[j2];
                 }
             }
         }
 
         return y;
     }
 
     private double innerProduct(double[] step, double[] grad) {
         double ip = 0.0;
         for (int i = 0; i < step.length; i++) {
             ip += step[i] * grad[i];
         }
         return ip;
     }
 
     public static double[] gmres(double[][] A, double[] f, double[] x0, int k) {
 //   r0 <- f - A %*% x0
         int n = f.length;
         int m = x0.length;
         double[] r0 = new double[n];
         for (int i = 0; i < n; i++) {
             r0[i] = f[i];
             for (int j = 0; j < m; j++) {
                 r0[i] -= A[i][j] * x0[j];
             }
         }
 //   v <- matrix(0,k+1,ncol(A))
         double[][] v = new double[k + 1][n];
 //   v[1,] <- r0 / sqrt(sum(r0*r0))
         v[0] = r0;
         double sumSqR0 = 0.0;
         for (int i = 0; i < n; i++) {
             sumSqR0 += r0[i] * r0[i];
         }
         sumSqR0 = Math.sqrt(sumSqR0);
         for (int i = 0; i < n; i++) {
             v[0][i] /= sumSqR0;
         }
 //   h <- matrix(0,k+1,k)
         double[][] h = new double[k + 1][k];
 //   for(j in 1:k) {
         for (int j = 0; j < k; j++) {
 //      for(i in 1:j)  {
             for (int i = 0; i <= j; i++) {
 //         h[i,j] <- t(A %*% v[j,]) %*% v[i,]
                 h[i][j] = 0;
                 for (int l = 0; l < n; l++) {
                     for (int l2 = 0; l2 < n; l2++) {
                         h[i][j] += v[j][l] * A[l2][l] * v[l2][i];
                     }
                 }
 //      }
             }
 //      v[j+1,] <- A %*% v[j,]
             for (int i = 0; i < n; i++) {
                 for (int l = 0; l < n; l++) {
                     v[j + 1][i] += A[i][l] * v[j][l];
                 }
             }
 //      for(i in 1:j) {
             for (int i = 0; i <= j; i++) {
 //        v[j+1,] <- v[j+1,] - h[i,j] * v[i,]
                 for (int l = 0; l < n; l++) {
                     v[j + 1][l] -= h[i][j] * v[i][l];
                 }
 //      }
             }
 //      h[j+1,j] <- sqrt(sum(v[j+1,]*v[j+1,]))
             h[j + 1][j] = 0;
             for (int i = 0; i < n; i++) {
                 h[j + 1][j] += v[j + 1][i] * v[j + 1][i];
             }
             h[j + 1][j] = Math.sqrt(h[j + 1][j]);
 //      v[j+1,] <- v[j+1,] / h[j+1,j]
             for (int i = 0; i < n; i++) {
                 v[j + 1][i] /= h[j + 1][j];
             }
 //   }
         }
 //   beta <- c(sqrt(sum(r0*r0)),rep(0,k-1))
         double[] beta = new double[k + 1];
         beta[0] = sumSqR0;
         // y is least squares solution to ||beta - Hy||
         final OLSMultipleLinearRegression lr = new OLSMultipleLinearRegression();
         lr.newSampleData(beta, h);
         final double[] y = lr.estimateRegressionParameters();
         // NB y = [intercept,x0,x1,...,xn]
 //   x0 + v[1:k,] %*% y
         double[] answer = new double[n];
         for (int i = 0; i < n; i++) {
             answer[i] = x0[i];
             for (int j = 0; j < k; j++) {
                 answer[i] += v[j][i] * y[j + 1];
             }
         }
         return answer;
     }
     double[] x0;
 
     public void gmresHessMult() {
 //   r0 <- f - A %*% x0
         double[] r0 = new double[d];
         double[] r0Tmp = spA != null ? hessVectMult(X, spA, x0, sv, svN, C) : hessVectMult(X, A, x0, sv, svN, C);
         for (int i = 0; i < d; i++) {
             r0[i] = -grad[i] - r0Tmp[i];
         }
         final int k = Math.min(d, minResIterations);
 //   v <- matrix(0,k+1,ncol(A))
         double[][] v = new double[k + 1][d];
 //   v[1,] <- r0 / sqrt(sum(r0*r0))
         v[0] = r0;
         double sumSqR0 = 0.0;
         for (int i = 0; i < d; i++) {
             sumSqR0 += r0[i] * r0[i];
         }
         sumSqR0 = Math.sqrt(sumSqR0);
         for (int i = 0; i < d; i++) {
             v[0][i] /= sumSqR0;
         }
 //   h <- matrix(0,k+1,k)
         double[][] h = new double[k + 1][k];
 //   for(j in 1:k) {
         for (int j = 0; j < k; j++) {
 //      for(i in 1:j)  {
             for (int i = 0; i <= j; i++) {
 //         h[i,j] <- t(A %*% v[j,]) %*% v[i,]
                 final double[] Avi = spA != null ? hessVectMult(X, spA, v[i], sv, svN, C) : hessVectMult(X, A, v[i], sv, svN, C);
                 for (int l = 0; l < d; l++) {
                     h[i][j] += v[j][l] * Avi[l];
                 }
 //      }
             }
 //      v[j+1,] <- A %*% v[j,]
             v[j + 1] = spA != null ? hessVectMult(X, spA, v[j], sv, svN, C) : hessVectMult(X,A,v[j],sv,svN,C);
 //      for(i in 1:j) {
             for (int i = 0; i <= j; i++) {
 //        v[j+1,] <- v[j+1,] - h[i,j] * v[i,]
                 for (int l = 0; l < d; l++) {
                     v[j + 1][l] -= h[i][j] * v[i][l];
                 }
 //      }
             }
 //      h[j+1,j] <- sqrt(sum(v[j+1,]*v[j+1,]))
             h[j + 1][j] = 0;
             for (int i = 0; i < d; i++) {
                 h[j + 1][j] += v[j + 1][i] * v[j + 1][i];
             }
             h[j + 1][j] = Math.sqrt(h[j + 1][j]);
 //      v[j+1,] <- v[j+1,] / h[j+1,j]
             for (int i = 0; i < d; i++) {
                 v[j + 1][i] /= h[j + 1][j];
             }
 //   }
         }
 //   beta <- c(sqrt(sum(r0*r0)),rep(0,k-1))
         double[] beta = new double[k + 1];
         beta[0] = sumSqR0;
         // y is least squares solution to ||beta - Hy||
         final OLSMultipleLinearRegression lr = new OLSMultipleLinearRegression();
         lr.newSampleData(beta, h);
         final double[] y = lr.estimateRegressionParameters();
         // NB y = [intercept,x0,x1,...,xn]
 //   x0 + v[1:k,] %*% y
         for (int i = 0; i < d; i++) {
             step[i] = x0[i];
             for (int j = 0; j < k; j++) {
                 step[i] += v[j][i] * y[j + 1];
             }
         }
     }

    private boolean allZero(double[] step) {
        for(int i = 0; i < step.length; i++) {
            if(step[i] != 0.0) {
                return false;
            }
        }
        return true;
    }
 }
