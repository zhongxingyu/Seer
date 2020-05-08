 package veeju.evaluators;
 
 import veeju.forms.Quote;
 import veeju.runtime.Object;
 
 /**
 * {@link QuoteEvaluator} is a functor that evaluates a {@link Quote} form.
  */
 public final class QuoteEvaluator implements Evaluator {
     /**
      * The quote form.
      *
      * @see #getQuote()
      */
     protected final Quote quote;
 
     /**
      * Creates a {@link QuoteEvaluator} instance.
      *
      * @param quote the quote form.
      */
     public QuoteEvaluator(final Quote quote) {
         if (quote == null) {
             throw new NullPointerException("quote cannot be null");
         }
         this.quote = quote;
     }
 
     /**
      * The getter method for {@link #quote}.
      *
      * @return the #quote.
      * @see #quote
      */
     public Quote getQuote() {
         return quote;
     }
 
     /**
      * The method for getting the quoted form.
      *
      * @return the quoted form.
      */
     public veeju.forms.Form getForm() {
         return getQuote().getForm();
     }
 
     public Object evaluate(final Object environment) {
         return environment.getRuntime().quote(this.getForm());
     }
 }
 
