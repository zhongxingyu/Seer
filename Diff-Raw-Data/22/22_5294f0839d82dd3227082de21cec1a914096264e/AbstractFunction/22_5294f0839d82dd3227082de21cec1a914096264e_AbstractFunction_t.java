 /*
  * Vitry, copyright (C) Hans Hoglund 2011
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * See COPYING.txt for details.
  */
 package vitry.runtime;
 
 import vitry.runtime.struct.Seq;
 
 
 /**
  * This class implements most aspects of a function, except invocation.
  * Requires scope upon construction, however domain and range are mutable 
  * to allow for in-place type reconstruction.
  *
  * @author Hans Hoglund 
  */
 abstract class AbstractFunction extends ConstructionPattern 
 implements Function, Scope, TypableFunction
 {
     final Env<Symbol, Object> environment;
 
     private Pattern domain;
 
     private Seq<Pattern> range;
 
 
     public AbstractFunction() {
         this(AbstractEnv.<Symbol, Object>empty());
     }
 
     public AbstractFunction(Scope scope) {
         this(scope.getValues(), null, null);
     }
 
     protected AbstractFunction(Env<Symbol, Object> env) {
         this(env, null, null);
     }
 
     protected AbstractFunction(Env<Symbol, Object> env, Pattern domain, Seq<Pattern> range) {
         this.environment = env;
         this.domain = domain;
         this.range = range;
     }
 
 
     public Object getValue(String name)
     {
         return getValue(Symbol.intern(name));
     }
 
     public Object getValue(Symbol name)
     {
         return environment.lookup(name);
     }
 
     public Env<Symbol, Object> getValues()
     {
         return this.environment;
     }
 
     public synchronized Pattern getDomain()
     {
         return domain;
     }
 
     public synchronized Seq<Pattern> getRange()
     {
         return range;
     }
 
     public synchronized void setDomain(Pattern domain)
     {
         this.domain = domain;
     }
 
     public synchronized void setRange(Seq<Pattern> range)
     {
         this.range = range;
     }
 
     public boolean isCompiled()
     {
         return true;
     }
     
     @Override
     public boolean eq(Function o)
     {
         return o == this;
     }
 
     @Override
     public boolean match(Function p)
     {
         return p == this;
     }
 
    public boolean eqFor(Pattern o)
     {
        return o.eq(this);
     }
 
    public boolean matchFor(Pattern p)
     {
        return p.match(this);
    }

    public boolean isInvertible()
    {
        return false;
     }
 
     public Pattern head()
     {
         return this.getDomain();
     }
 
 
     public Seq<Pattern> tail()
     {
         return this.getRange();
     }
     
     public boolean isNil()
     {
         return false;
     }
 
     public boolean hasTail()
     {
         return this.getRange() != null;
     }
 
     public String toString()
     {
         if (domain == null)
             return super.toString();
         if (range == null)
             return domain.toString();
         else
             return domain.toString() + " -> " + range.toString();
     }
 
 
     protected static int checkArity(int arity)
     {
         if (arity < VitryRuntime.MIN_ARITY || arity > VitryRuntime.MAX_ARITY)
             throw new IllegalArgumentException("Function must have arity a where "
                     + VitryRuntime.MIN_ARITY + " < a < " + VitryRuntime.MAX_ARITY);
         return arity;
     }
 
 
 }
