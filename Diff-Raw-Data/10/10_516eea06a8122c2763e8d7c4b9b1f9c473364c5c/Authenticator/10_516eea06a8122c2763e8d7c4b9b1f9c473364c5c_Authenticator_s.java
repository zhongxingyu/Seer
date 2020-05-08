 package com.atlassian.sal.api.auth;
 
 import java.security.Principal;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.atlassian.sal.api.message.Message;
 
 /**
  * Authenticates requests
  *
  * @since 2.0
  */
 public interface Authenticator
 {
     /**
      * Authenticates a request
      * @param request The request
      * @param response The response
      * @return The result of the authentication
      */
     Result authenticate(HttpServletRequest request, HttpServletResponse response);
 
     /**
      * Encapsulates the results of an authentication attempt.  Includes the result status, any problem that
      * occurred, and possibly the authenticated users {@link Principal}.
      */
     static class Result
     {
         private final Result.Status status;
         private final Message message;
         private final Principal principal;
 
         Result(final Result.Status status)
         {
             this(status, null, null);
         }
 
         Result(final Result.Status status, final Message message)
         {
             this(status, message, null);
         }
 
         Result(final Result.Status status, final Principal principal)
         {
             this(status, null, principal);
         }
 
         Result(final Result.Status status, final Message message, final Principal principal)
         {
             if (status == null)
             {
                throw new IllegalArgumentException("status");
            }
            if (message == null)
            {
                throw new IllegalArgumentException("message");
            }
            if (principal == null)
            {
                throw new IllegalArgumentException("principal");
             }
             this.status = status;
             this.message = message;
             this.principal = principal;
         }
 
         public Result.Status getStatus()
         {
             return status;
         }
 
         public String getMessage()
         {
             return message.toString();
         }
 
         public Principal getPrincipal()
         {
             return principal;
         }
 
         public static enum Status
         {
             SUCCESS("success"),
             FAILED("failed"),
             ERROR("error"),
             NO_ATTEMPT("no attempt");
 
             private final String name;
 
             private Status(final String name)
             {
                 this.name = name;
             }
 
             @Override
             public String toString()
             {
                 return name;
             }
         }
 
         public static final class NoAttempt extends Result
         {
             public NoAttempt()
             {
                 super(Status.NO_ATTEMPT);
             }
         }
 
         public static final class Error extends Result
         {
             public Error(final Message message)
             {
                 super(Status.ERROR, message);
             }
         }
 
         public static final class Failure extends Result
         {
             public Failure(final Message message)
             {
                 super(Status.FAILED, message);
             }
         }
 
         public static final class Success extends Result
         {
             public Success(final Principal principal)
             {
                 super(Status.SUCCESS, principal);
             }
         }
     }
 }
