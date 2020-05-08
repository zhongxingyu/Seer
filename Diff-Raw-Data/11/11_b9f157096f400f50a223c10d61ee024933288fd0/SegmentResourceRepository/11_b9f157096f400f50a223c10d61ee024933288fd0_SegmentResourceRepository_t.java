 package org.jcwal.so.network.repository;
 
 import java.util.List;
 
 import org.jcwal.so.network.domain.SegmentResource;
 import org.macula.core.repository.MaculaJpaRepository;
 import org.springframework.data.jpa.repository.Query;
 import org.springframework.data.repository.query.Param;
 
 public interface SegmentResourceRepository extends MaculaJpaRepository<SegmentResource, Long> {
 
	@Query("from SegmentResource t where t.tstartIp like :segment or t.tsegment like :segment or t.tendIp like :segment")
 	List<SegmentResource> findIpBelongs(@Param("segment") String segment);
 
 	@Query("from SegmentResource t where t.id in :ids")
 	List<SegmentResource> findIds(@Param("ids") List<Long> ids);
 }
