 package com.example.stocks.core;
 
 import com.example.stocks.infrastructure.SystemConfiguration;
 import com.example.stocks.infrastructure.http.HttpClient;
 import com.example.stocks.infrastructure.http.HttpClientFactory;
 import com.example.stocks.infrastructure.server.HttpApplicationServerBuilder;
 import com.example.stocks.infrastructure.server.PortfolioBuilder;
 import com.example.stocks.infrastructure.server.Server;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import static com.example.stocks.core.ExampleStocks.Amazon;
 import static com.example.stocks.core.ExampleStocks.Apple;
 import static com.example.stocks.infrastructure.server.ApplicationBuilder.defaultApplication;
 import static com.example.stocks.infrastructure.server.HttpApplicationServerBuilder.defaultHttpServer;
 import static com.example.stocks.infrastructure.server.PortfolioBuilder.defaultPortfolio;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 public class PortfolioSystemTest {
 
     private final Book book = new StubBook().add(Amazon).add(Apple);
     private final MarketData marketData = new StubMarketData().add(Amazon, new Money(270)).add(Apple, new Money(450));
     private final PortfolioBuilder portfolio = defaultPortfolio().with(book).with(marketData);
     private final HttpApplicationServerBuilder httpServer = defaultHttpServer().with(portfolio);
     private final Server application = defaultApplication().with(httpServer).build();
 
     @Before
     public void startServer() {
         application.start();
     }
 
     @Test
     public void valuation() throws MalformedURLException {
         HttpClient http = new HttpClientFactory(new SystemConfiguration()).createClient();
         String response = http.get(new URL("http://localhost:8000/portfolio/0001"));
        assertThat(response, is("720"));
     }
 
     @After
     public void stopServer() {
         application.stop();
     }
 
 }
