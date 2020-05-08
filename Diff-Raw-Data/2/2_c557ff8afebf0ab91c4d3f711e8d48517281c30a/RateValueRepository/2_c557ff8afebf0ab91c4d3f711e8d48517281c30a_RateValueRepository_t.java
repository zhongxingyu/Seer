 package org.gsoft.openserv.repositories.rates;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.gsoft.openserv.domain.rates.RateValue;
 import org.gsoft.openserv.domain.rates.Rate;
 import org.gsoft.openserv.repositories.BaseRepository;
 import org.gsoft.openserv.repositories.BaseSpringRepository;
 import org.springframework.data.jpa.repository.Query;
 import org.springframework.data.repository.query.Param;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class RateValueRepository extends BaseRepository<RateValue, Long>{
 	@Resource
 	private RateValueSpringRepository rateValueSpringRepository;
 
 	@Override
 	protected BaseSpringRepository<RateValue, Long> getSpringRepository() {
 		return rateValueSpringRepository;
 	}
 
 	public RateValue findRateValue(Rate rate, Date date){
 		return this.rateValueSpringRepository.findRateValue(rate, date);
 	}
 
 	public RateValue findRateValueByTickerSymbol(String tickerSymbol, Date date){
 		return this.rateValueSpringRepository.findRateValueByTickerSymbol(tickerSymbol, date);
 	}
 
 	public RateValue findMostRecentQuote(Rate rate){
 		return this.rateValueSpringRepository.findMostRecentQuote(rate);
 	}
 	
 	public List<Object[]> findAllQuotesForDate(Date date){
 		return this.rateValueSpringRepository.findAllQuotesForDate(date);
 	}
 	
 	public RateValue findCurrentRateAsOf(Rate rate, Date date){
 		return this.rateValueSpringRepository.findCurrentRateAsOf(rate, date);
 	}
 }
 
 @Repository
 interface RateValueSpringRepository extends BaseSpringRepository<RateValue, Long>{
 	
	@Query("select rateValue from RateValue rateValue where rateValue.rate = :rate and rateValue.rateValueDate = (select max(rateValue.rateValueDate) from RateValue rateValue where rateValue.rateValueDate <= :rateValueDate)")
 	public RateValue findRateValue(@Param("rate") Rate rate, @Param("rateValueDate") Date date);
 
 	@Query("select rateValue from RateValue rateValue where rateValue.rate.tickerSymbol = :tickerSymbol and rateValue.rateValueDate <= :rateValueDate")
 	public RateValue findRateValueByTickerSymbol(@Param("tickerSymbol") String tickerSymbol, @Param("rateValueDate") Date date);
 
 	@Query("select rateValue from RateValue rateValue where rateValue.rate = :rate and rateValue.rateValueDate = ( " +
 			"select max(rateValueDate) from RateValue rateValue2 where rateValue2.rate = :rate)")
 	public RateValue findMostRecentQuote(@Param("rate") Rate rate);
 	
 	@Query("select rate,rateValue from RateValue rateValue right outer join rateValue.rate rate where rateValue is null or rateValue.rateValueDate = :rateValueDate")
 	public List<Object[]> findAllQuotesForDate(@Param("rateValueDate") Date date);
 	
 	@Query("select rateValue from RateValue rateValue where rateValue.rate = :rate and rateValue.rateValueDate = ( " +
 			"select max(q2.rateValueDate) from RateValue q2 where q2.rate = :rate and q2.rateValueDate <= :date)")
 	public RateValue findCurrentRateAsOf(@Param("rate") Rate rate, @Param("date")Date date);
 }
