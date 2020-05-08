 
 package me.heldplayer.web.server.internal;
 
 import java.io.IOException;
 
 import me.heldplayer.web.server.RequestSource;
 
 public class ErrorResponse extends WebResponse {
 
     private ErrorType type;
 
     public ErrorResponse(ErrorType type) throws IOException {
         super();
         this.type = type;
     }
 
     @Override
     public WebResponse writeResponse(RequestSource source) throws IOException {
         this.header.writeBytes("HTTP/1.0 " + this.type.code + " " + this.type.reason + "\r\n");
         this.header.writeBytes("Connection: close\r\n");
        this.header.writeBytes("Server: HeldBot\r\n");
         this.header.writeBytes("Content-Type: text/plain\r\n");
 
         if (source == null || source.method.hasBody) {
             this.body.writeBytes("Error code " + this.type.code + " - " + this.type.reason + "\r\n");
             this.body.writeBytes("Unable to finish request\r\n");
         }
 
         return this;
     }
 
     public static enum ErrorType {
         BadRequest("400", "Bad Request"),
         Unauthorized("401", "Unauthorized"),
         PaymentRequired("402", "Payment Required"),
         Forbidden("403", "Forbidden"),
         NotFound("404", "Not Found"),
         MethodNotAllowed("405", "Method Not Allowed"),
         NotAcceptable("406", "Not Acceptable"),
         ProxyAuthenticationRequired("407", "Proxy Authentication Required"),
         RequestTimeout("408", "Request Time-out"),
         Conflict("409", "Conflict"),
         Gone("410", "Gone"),
         LengthRequired("411", "Length Required"),
         PreconditionFailed("412", "Precondition Failed"),
         RequestEntityTooLarge("413", "Request Entity Too Large"),
         RequestURITooLarge("414", "Request-URI Too Large"),
         UnsupportedMediaType("415", "Unsupported Media Type"),
         RequestRangeNotSatisfiable("416", "Request Range Not Satisfiable"),
         ExpectationFailed("417", "Expectation Failed"),
         InternalServerError("500", "Internal Server Error"),
         NotImplemented("501", "Not Implemented"),
         BadGateway("502", "Bad Gateway"),
         ServiceUnavailable("503", "Service Unavailable"),
         GatewayTimeout("504", "Gateway Time-out"),
         HTTPVersionNotSupported("505", "HTTP Version Not Supported");
 
         public final String code, reason;
 
         private ErrorType(String code, String reason) {
             this.code = code;
             this.reason = reason;
         }
     }
 
 }
