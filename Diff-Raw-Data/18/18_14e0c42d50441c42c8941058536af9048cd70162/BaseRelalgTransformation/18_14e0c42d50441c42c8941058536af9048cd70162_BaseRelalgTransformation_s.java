 package edu.rivfader.relalg;
 
 public abstract class BaseRelalgTransformation<R>
     implements IRelAlgExprTransformation<R> {
     @Override
     public R transform(IRelAlgExpr input) {
         R o; // output
         if (input instanceof Product) {
             o = transformProduct((Product) input);
         } else if (input instanceof Projection) {
             o = transformProjection((Projection) input);
         } else if (input instanceof Selection) {
             o = transformSelection((Selection) input);
         } else if (input instanceof LoadTable) {
             o = transformLoadTable((LoadTable) input);
         } else if (input instanceof RenameTable) {
             o = transformRenameTable((RenameTable) input);
         } else if (input instanceof IStubResult) {
             o = transformStubResult((IStubResult<R>) input);
         } else {
             throw new IllegalArgumentException("Unexpected IRelAlgExpr "
                                                 + input.toString());
         }
         return o;
     }
 
     public abstract R transformProduct(Product p);
     public abstract R transformProjection(Projection p);
     public abstract R transformSelection(Selection s);
     public abstract R transformLoadTable(LoadTable l);
     public abstract R transformRenameTable(RenameTable r);
 
     protected R transformStubResult(IStubResult<R> sr) {
         return sr.getExpectedResult();
     }
 }
